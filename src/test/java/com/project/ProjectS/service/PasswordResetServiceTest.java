package com.project.ProjectS.service;

import com.project.ProjectS.entity.PasswordResetToken;
import com.project.ProjectS.entity.User;
import com.project.ProjectS.repository.PasswordResetTokenRepository;
import com.project.ProjectS.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.web.server.ResponseStatusException;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PasswordResetServiceTest {
    private final UserRepository users = mock(UserRepository.class);
    private final PasswordResetTokenRepository tokens = mock(PasswordResetTokenRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final PasswordResetService service = new PasswordResetService(
            users, tokens, encoder, mailSender, "http://localhost:5173", "sender@example.com", 15);

    @Test
    void generatedTokensAreUnpredictableUrlSafeAndAtLeast32Bytes() {
        String first = PasswordResetService.generateToken();
        String second = PasswordResetService.generateToken();
        assertNotEquals(first, second);
        assertTrue(first.matches("[A-Za-z0-9_-]{43}"));
        assertEquals(64, PasswordResetService.hashToken(first).length());
        assertNotEquals(PasswordResetService.hashToken(first), PasswordResetService.hashToken(second));
    }

    @Test
    void successfulResetHashesNewPasswordAndConsumesToken() {
        String rawToken = PasswordResetService.generateToken();
        User user = new User();
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(10));
        when(tokens.findByTokenHashForUpdate(PasswordResetService.hashToken(rawToken))).thenReturn(Optional.of(token));
        when(encoder.encode("NewStrongPassword123!")).thenReturn("bcrypt-hash");

        service.resetPassword(rawToken, "NewStrongPassword123!");

        assertEquals("bcrypt-hash", user.getPassword());
        assertTrue(token.isUsed());
        verify(users).save(user);
        verify(tokens).save(token);
        verify(tokens).deleteUnusedForUser(user.getUserId());
    }

    @Test
    void expiredAndUsedTokensAreRejected() {
        String rawToken = PasswordResetService.generateToken();
        PasswordResetToken expired = new PasswordResetToken();
        expired.setExpiryDate(LocalDateTime.now().minusSeconds(1));
        when(tokens.findByTokenHashForUpdate(PasswordResetService.hashToken(rawToken))).thenReturn(Optional.of(expired));
        assertThrows(ResponseStatusException.class, () -> service.resetPassword(rawToken, "NewStrongPassword123!"));

        PasswordResetToken used = new PasswordResetToken();
        used.setExpiryDate(LocalDateTime.now().plusMinutes(5));
        used.setUsed(true);
        when(tokens.findByTokenHashForUpdate(PasswordResetService.hashToken(rawToken))).thenReturn(Optional.of(used));
        assertThrows(ResponseStatusException.class, () -> service.resetPassword(rawToken, "NewStrongPassword123!"));
        verifyNoInteractions(encoder);
    }

    @Test
    void unknownTokenIsRejected() {
        when(tokens.findByTokenHashForUpdate(any())).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class,
                () -> service.resetPassword("unknown-token", "NewStrongPassword123!"));
        verifyNoInteractions(encoder);
    }

    @Test
    void forgotPasswordStoresOnlyAHashAndEmailsTheRawTokenForRegisteredAccounts() {
        User user = new User();
        user.setUserId(42L);
        user.setEmail("student@example.com");
        when(users.findByEmailIgnoreCase("student@example.com")).thenReturn(Optional.of(user));
        service.requestReset("student@example.com");

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokens).save(tokenCaptor.capture());
        PasswordResetToken stored = tokenCaptor.getValue();
        assertEquals(64, stored.getTokenHash().length());
        assertFalse(stored.isUsed());
        assertTrue(stored.getExpiryDate().isAfter(LocalDateTime.now().plusMinutes(14)));
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals("student@example.com", sentMessage.getTo()[0]);
        assertTrue(sentMessage.getText().contains("/reset-password?token="));
        assertFalse(sentMessage.getText().contains(stored.getTokenHash()));
        verify(tokens).deleteUnusedForUser(42L);
    }

    @Test
    void forgotPasswordDoesNotCreateTokenOrSendEmailForUnknownAccounts() {
        when(users.findByEmailIgnoreCase("unknown@example.com")).thenReturn(Optional.empty());

        service.requestReset("unknown@example.com");

        verifyNoInteractions(tokens, mailSender);
    }
}

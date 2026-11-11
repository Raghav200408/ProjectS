package com.project.ProjectS.service;

import com.project.ProjectS.entity.PasswordResetToken;
import com.project.ProjectS.entity.User;
import com.project.ProjectS.repository.PasswordResetTokenRepository;
import com.project.ProjectS.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class PasswordResetService {
    private static final Logger log = LogManager.getLogger(PasswordResetService.class);
    public static final String GENERIC_FORGOT_RESPONSE =
            "If an account exists with this email, a password reset link has been sent.";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private final UserRepository users;
    private final PasswordResetTokenRepository tokens;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final String frontendUrl;
    private final String fromAddress;
    private final long ttlMinutes;

    public PasswordResetService(UserRepository users, PasswordResetTokenRepository tokens,
                                PasswordEncoder passwordEncoder, JavaMailSender mailSender,
                                @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl,
                                @Value("${spring.mail.username:}") String fromAddress,
                                @Value("${app.password-reset.token-ttl-minutes:15}") long ttlMinutes) {
        this.users = users;
        this.tokens = tokens;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.frontendUrl = frontendUrl;
        this.fromAddress = fromAddress;
        this.ttlMinutes = ttlMinutes;
    }

    @Transactional
    public void requestReset(String email) {
        users.findByEmailIgnoreCase(email.trim()).ifPresent(user -> {
            String rawToken = generateToken();
            PasswordResetToken token = new PasswordResetToken();
            token.setUser(user);
            token.setTokenHash(hashToken(rawToken));
            token.setExpiryDate(LocalDateTime.now().plusMinutes(ttlMinutes));
            token.setUsed(false);
            tokens.deleteUnusedForUser(user.getUserId());
            tokens.save(token);
            sendResetLink(user, rawToken);
        });
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken token = tokens.findByTokenHashForUpdate(hashToken(rawToken))
                .filter(candidate -> !candidate.isUsed() && candidate.getExpiryDate().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Reset token is invalid or expired."));
        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setCredentialVersion(user.getCredentialVersion() + 1);
        users.save(user);
        token.setUsed(true);
        tokens.save(token);
        tokens.deleteUnusedForUser(user.getUserId());
    }

    @Scheduled(fixedDelayString = "${app.password-reset.cleanup-ms:3600000}", initialDelay = 60000)
    @Transactional
    public void cleanupTokens() {
        tokens.deleteExpiredOrUsed(LocalDateTime.now());
    }

    private boolean sendResetLink(User user, String rawToken) {
        URI configuredFrontend = URI.create(frontendUrl);
        String host = configuredFrontend.getHost();
        boolean localFrontend = "localhost".equalsIgnoreCase(host)
                || "127.0.0.1".equals(host) || "::1".equals(host);
        UriComponentsBuilder linkBuilder = UriComponentsBuilder.fromUriString(frontendUrl);
        if (!localFrontend) linkBuilder.scheme("https");
        String link = linkBuilder.path("/reset-password")
                .queryParam("token", rawToken)
                .build().encode().toUriString();

        SimpleMailMessage message = new SimpleMailMessage();
        if (!fromAddress.isBlank()) message.setFrom(fromAddress);
        message.setTo(user.getEmail());
        message.setSubject("Password Reset Request");
        message.setText("Hello,\n\nWe received a request to reset your password.\n\n"
                + "Click the link below to create a new password:\n\n" + link + "\n\n"
                + "This link expires in 15 minutes.\n\n"
                + "If you did not request a password reset, please ignore this email.\n\nRegards,\nSupport Team");
        try {
            mailSender.send(message);
            return true;
        } catch (MailException ex) {
            // Avoid logging the address, reset URL, or provider exception details.
            log.warn("Password reset email delivery failed ({})", ex.getClass().getSimpleName());
            return false;
        }
    }

    static String generateToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }
}

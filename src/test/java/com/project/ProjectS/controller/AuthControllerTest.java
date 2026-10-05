package com.project.ProjectS.controller;

import com.project.ProjectS.entity.Role;
import com.project.ProjectS.entity.User;
import com.project.ProjectS.repository.UserRepository;
import com.project.ProjectS.security.jwt.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {
    private final UserRepository users = mock(UserRepository.class);
    private final AuthController controller = new AuthController(
            mock(AuthenticationManager.class), users, mock(JwtUtil.class));

    @Test
    void profileComesFromAuthenticatedIdentityWithoutReturningCredentials() {
        User user = mock(User.class);
        Role role = mock(Role.class);
        when(users.findByEmail("student@example.com")).thenReturn(Optional.of(user));
        when(user.getUserId()).thenReturn(7L);
        when(user.getName()).thenReturn("Student");
        when(user.getEmail()).thenReturn("student@example.com");
        when(user.getRole()).thenReturn(role);
        when(role.getRoleName()).thenReturn("STUDENT");

        var profile = controller.currentUser(() -> "student@example.com").getBody();

        assertNotNull(profile);
        assertEquals(7L, profile.userId());
        assertEquals("Student", profile.name());
        assertEquals("student@example.com", profile.email());
        assertEquals("STUDENT", profile.role());
        verify(users).findByEmail("student@example.com");
    }

    @Test
    void missingPrincipalIsRejectedWithoutQueryingUsers() {
        var error = assertThrows(ResponseStatusException.class,
                () -> controller.currentUser(null));
        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
        verifyNoInteractions(users);
    }

    @Test
    void deletedUserIsRejected() {
        when(users.findByEmail("deleted@example.com")).thenReturn(Optional.empty());
        var error = assertThrows(ResponseStatusException.class,
                () -> controller.currentUser(() -> "deleted@example.com"));
        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
    }
}

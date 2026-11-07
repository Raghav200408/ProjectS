package com.project.ProjectS.controller;

import com.project.ProjectS.entity.User;
import com.project.ProjectS.config.AuditLogger;
import com.project.ProjectS.model.LoginRequestDTO;
import com.project.ProjectS.model.LoginResponseDTO;
import com.project.ProjectS.model.UserProfileDTO;
import com.project.ProjectS.model.PasswordVerificationRequestDTO;
import com.project.ProjectS.model.ChangePasswordRequestDTO;
import com.project.ProjectS.repository.UserRepository;
import com.project.ProjectS.security.jwt.JwtUtil;

import jakarta.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger log = LogManager.getLogger(AuthController.class);

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository, JwtUtil jwtUtil) {
        this(authenticationManager, userRepository, jwtUtil, new AuditLogger(), new BCryptPasswordEncoder(12));
    }

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository, JwtUtil jwtUtil, AuditLogger auditLogger, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.auditLogger = auditLogger;
        this.passwordEncoder = passwordEncoder;
    }

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuditLogger auditLogger;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/me")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<UserProfileDTO> currentUser(Principal principal) {
        log.info("Fetching current user profile");
        if (principal == null) {
            log.warn("Current user request missing principal");
            auditLogger.log("PROFILE_FETCH", null, "USER", null, "UNAUTHORIZED", "/api/auth/me");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> {
                    log.warn("Current user profile lookup failed for principal={}", principal.getName());
                    auditLogger.log("PROFILE_FETCH", principal.getName(), "USER", null, "NOT_FOUND", "/api/auth/me");
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED);
                });
        log.info("Returning current user profile for userId={} email={}", user.getUserId(), user.getEmail());
        auditLogger.log("PROFILE_FETCH", user.getEmail(), "USER", user.getUserId(), "SUCCESS", "/api/auth/me");
        return ResponseEntity.ok(UserProfileDTO.from(user));
    }

    @PostMapping("/password/verify")
    public ResponseEntity<Map<String, String>> verifyCurrentPassword(
            Principal principal, @Valid @RequestBody PasswordVerificationRequestDTO request) {
        User user = getAuthenticatedUser(principal);
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This account does not have a password.");
        }
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect password");
        }
        return ResponseEntity.ok(Map.of("message", "Current password verified."));
    }

    @PutMapping("/password")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<Map<String, String>> changePassword(
            Principal principal, @Valid @RequestBody ChangePasswordRequestDTO request) {
        User user = getAuthenticatedUser(principal);
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This account does not have a password.");
        }
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect password");
        }
        if (request.getNewPassword().equals(request.getCurrentPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a new password different from your current password.");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        auditLogger.log("PASSWORD_CHANGE", user.getEmail(), "USER", user.getUserId(), "SUCCESS");
        return ResponseEntity.ok(Map.of("message", "Password changed successfully."));
    }

    private User getAuthenticatedUser(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request) {
        log.info("Login attempt received for email={}", request.getEmail());
        auditLogger.log("LOGIN", request.getEmail(), "USER", null, "ATTEMPT", "email/password login");

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (Exception ex) {
            log.warn("Login failed for email={} due to invalid credentials", request.getEmail(), ex);
            auditLogger.log("LOGIN", request.getEmail(), "USER", null, "FAILED", "invalid credentials");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: user not found for email={}", request.getEmail());
                    auditLogger.log("LOGIN", request.getEmail(), "USER", null, "FAILED", "user not found");
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
                });

        user.setLastLoginAt(java.time.LocalDateTime.now());
        userRepository.save(user);

        String roleName =
                user.getRole().getRoleName();

        String authority =
                "ROLE_" + roleName;

        String token =
                jwtUtil.generateToken(
                        user.getEmail(),
                        authority
                );

        LoginResponseDTO response =
                new LoginResponseDTO();

        response.setToken(token);
        response.setTokenType("Bearer");

        response.setUserId(user.getUserId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(roleName);

        log.info("Login successful for userId={} email={} role={}", user.getUserId(), user.getEmail(), roleName);
        auditLogger.log("LOGIN", user.getEmail(), "USER", user.getUserId(), "SUCCESS", "email/password login");
        return ResponseEntity.ok(response);
    }
}

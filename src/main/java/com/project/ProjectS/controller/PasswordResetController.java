package com.project.ProjectS.controller;

import com.project.ProjectS.model.ForgotPasswordRequestDTO;
import com.project.ProjectS.model.ResetPasswordRequestDTO;
import com.project.ProjectS.service.ForgotPasswordRateLimiter;
import com.project.ProjectS.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {
    private final PasswordResetService passwordResetService;
    private final ForgotPasswordRateLimiter rateLimiter;

    public PasswordResetController(PasswordResetService passwordResetService, ForgotPasswordRateLimiter rateLimiter) {
        this.passwordResetService = passwordResetService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDTO request, HttpServletRequest httpRequest) {
        if (!rateLimiter.allow(request.getEmail(), httpRequest.getRemoteAddr())) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Too many password reset requests. Please try again later.");
        }
        passwordResetService.requestReset(request.getEmail());
        return ResponseEntity.ok(Map.of("message", PasswordResetService.GENERIC_FORGOT_RESPONSE));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password has been reset successfully."));
    }
}

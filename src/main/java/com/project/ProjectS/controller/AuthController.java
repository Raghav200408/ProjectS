package com.project.ProjectS.controller;

import com.project.ProjectS.entity.User;
import com.project.ProjectS.model.LoginRequestDTO;
import com.project.ProjectS.model.LoginResponseDTO;
import com.project.ProjectS.repository.UserRepository;
import com.project.ProjectS.security.jwt.JwtUtil;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;


    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request) {

        // 1. Verify email + password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Get user from database
        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        // 3. Get role
        String roleName =
                user.getRole().getRoleName();

        String authority =
                "ROLE_" + roleName;

        // 4. Generate JWT
        String token =
                jwtUtil.generateToken(
                        user.getEmail(),
                        authority
                );

        // 5. Build response
        LoginResponseDTO response =
                new LoginResponseDTO();

        response.setToken(token);
        response.setTokenType("Bearer");

        response.setUserId(user.getUserId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(roleName);

        return ResponseEntity.ok(response);
    }
}

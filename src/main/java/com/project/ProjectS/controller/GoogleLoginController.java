package com.project.ProjectS.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.ProjectS.model.GoogleUserDTO;
import com.project.ProjectS.model.LoginResponseDTO;
import com.project.ProjectS.model.UserDTO;
import com.project.ProjectS.security.jwt.JwtUtil;
import com.project.ProjectS.service.GoogleOAuthService;
import com.project.ProjectS.service.UserService;

@RestController
public class GoogleLoginController {

    @Autowired
    private GoogleOAuthService googleOAuthService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/user")
    public LoginResponseDTO login(
            @AuthenticationPrincipal OAuth2User oauthUser) {

        if (oauthUser == null) {
            throw new RuntimeException("Google user not authenticated.");
        }

        GoogleUserDTO googleUser =
                googleOAuthService.getGoogleUser(oauthUser);

        UserDTO existingUser =
                userService.findByEmail(googleUser.getEmail());

        LoginResponseDTO response =
                new LoginResponseDTO();

        if (existingUser != null) {

            String role = switch (existingUser.getRoleId()) {
                case 1 -> "ROLE_SUPER_ADMIN";
                case 2 -> "ROLE_BRANCH_ADMIN";
                default -> "ROLE_STUDENT";
            };

            String token =
                    jwtUtil.generateToken(
                            existingUser.getEmail(),
                            role
                    );

            response.setRegistered(true);
            response.setUser(existingUser);
            response.setToken(token);

        } else {

            response.setRegistered(false);
            response.setGoogleUser(googleUser);

        }

        return response;
    }

}
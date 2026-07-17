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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Google Authentication", description = "Google OAuth Login APIs")
public class GoogleLoginController {

    @Autowired
    private GoogleOAuthService googleOAuthService;

    @Autowired
    private UserService userService;

<<<<<<< Updated upstream
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
=======
    @Operation(
        summary = "Get logged-in Google user details",
        description = "Returns the registered user if found; otherwise returns Google user information."
    )
    @ApiResponse(responseCode = "200", description = "Login successful")
    @GetMapping("/user")
    public LoginResponseDTO login(@AuthenticationPrincipal OAuth2User oauthUser) {
>>>>>>> Stashed changes

        System.out.println("Google Controller Called");

        GoogleUserDTO googleUser = googleOAuthService.getGoogleUser(oauthUser);

        UserDTO existingUser = userService.findByEmail(googleUser.getEmail());

        LoginResponseDTO response =
                new LoginResponseDTO();

        if (existingUser != null) {
<<<<<<< Updated upstream

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

=======
            response.setRegistered(true);
            response.setUser(existingUser);
>>>>>>> Stashed changes
        } else {
            response.setRegistered(false);
            response.setGoogleUser(googleUser);
        }

        return response;
    }

}
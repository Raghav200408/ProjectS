package com.project.ProjectS.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.ProjectS.model.GoogleUserDTO;
import com.project.ProjectS.model.LoginResponseDTO;
import com.project.ProjectS.model.UserDTO;
import com.project.ProjectS.service.GoogleOAuthService;
import com.project.ProjectS.service.UserService;

@RestController
public class GoogleLoginController {

    @Autowired
    private GoogleOAuthService googleOAuthService;

    @Autowired
    private UserService userService;

    @GetMapping("/user")
    public LoginResponseDTO login(
            @AuthenticationPrincipal OAuth2User oauthUser) {
    	System.out.println("Google Controller Called");
        GoogleUserDTO googleUser =
                googleOAuthService.getGoogleUser(oauthUser);

        UserDTO existingUser =
                userService.findByEmail(googleUser.getEmail());

        LoginResponseDTO response = new LoginResponseDTO();

        if (existingUser != null) {

            response.setRegistered(true);
            response.setUser(existingUser);

        } else {

            response.setRegistered(false);
            response.setGoogleUser(googleUser);

        }

        return response;
    }
}
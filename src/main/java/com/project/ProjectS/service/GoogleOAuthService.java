package com.project.ProjectS.service;

import java.util.Map;

import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.project.ProjectS.model.GoogleUserDTO;

@Service
public class GoogleOAuthService {

    public GoogleUserDTO getGoogleUser(OAuth2User user) {

        Map<String, Object> attributes = user.getAttributes();

        GoogleUserDTO dto = new GoogleUserDTO();

        dto.setGoogleId((String) attributes.get("sub"));
        dto.setName((String) attributes.get("name"));
        dto.setEmail((String) attributes.get("email"));
        dto.setPicture((String) attributes.get("picture"));
        dto.setEmailVerified((Boolean) attributes.get("email_verified"));

        return dto;
    }
}
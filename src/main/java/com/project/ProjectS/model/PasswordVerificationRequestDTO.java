package com.project.ProjectS.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordVerificationRequestDTO {
    @NotBlank(message = "Current password is required.")
    private String currentPassword;
}

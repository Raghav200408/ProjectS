package com.project.ProjectS.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.nio.charset.StandardCharsets;

@Getter
@Setter
public class ChangePasswordRequestDTO {
    @NotBlank(message = "Current password is required.")
    private String currentPassword;

    @NotBlank(message = "New password is required.")
    @Pattern(regexp = "(?s)^(?=.{8,}$)(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[^A-Za-z0-9\\s]).*$",
            message = "Use at least 8 characters, including uppercase, lowercase, a number and a special character.")
    private String newPassword;

    @NotBlank(message = "Please confirm your new password.")
    private String confirmPassword;

    @AssertTrue(message = "Password is too long (maximum 72 UTF-8 bytes).")
    public boolean isNewPasswordWithinEncodingLimit() {
        return newPassword == null || newPassword.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    @AssertTrue(message = "New password and confirmation do not match.")
    public boolean isConfirmationMatching() {
        return newPassword == null || confirmPassword == null || newPassword.equals(confirmPassword);
    }
}

package com.project.ProjectS.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.nio.charset.StandardCharsets;

@Getter
@Setter
public class ResetPasswordRequestDTO {
    @NotBlank(message = "Reset token is required.")
    @Size(max = 512)
    private String token;

    @NotBlank(message = "New password is required.")
    @Pattern(regexp = "(?s)^(?=.{8,}$)(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[^A-Za-z0-9\\s]).*$",
            message = "Use at least 8 characters, including uppercase, lowercase, a number and a special character.")
    private String newPassword;

    @AssertTrue(message = "Password is too long (maximum 72 UTF-8 bytes).")
    public boolean isNewPasswordWithinEncodingLimit() {
        return newPassword == null || newPassword.getBytes(StandardCharsets.UTF_8).length <= 72;
    }
}

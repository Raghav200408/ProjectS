package com.project.ProjectS.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.nio.charset.StandardCharsets;

@Getter
@Setter
public class GuestUserRequestDTO {
    private String name;
    @NotBlank(message = "Password is required.")
    @Pattern(regexp = "(?s)^(?=.{8,}$)(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[^A-Za-z0-9\\s]).*$",
            message = "Use at least 8 characters, including uppercase, lowercase, a number and a special character.")
    private String password;
    @NotBlank(message = "Email address is required.")
    @Email(message = "Enter a valid email address.")
    @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Enter a valid email address.")
    private String email;
    private String address;
    private String phoneNumber;

    @AssertTrue(message = "Password is too long (maximum 72 UTF-8 bytes).")
    public boolean isPasswordWithinEncodingLimit() {
        return password == null || password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }
}
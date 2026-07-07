package com.bnp.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request/response payloads for the auth endpoints.
 */
public class AuthDtos {

    public record RegisterRequest(
            @NotBlank(message = "Full name is required") String fullName,
            @NotBlank
            @Email(message = "Valid email is required") String email,
            @NotBlank
            @Size(min = 8, message = "Password must be at least 8 characters") String password,
            String phone,
            String profession,
            String city,
            String state,
            Double latitude,
            Double longitude,
            String referralCode // optional: app-referral invite code
            ) {

    }

    public record LoginRequest(
            @NotBlank
            @Email(message = "Valid email is required") String email,
            @NotBlank(message = "Password is required") String password
            ) {

    }

    public record AuthResponse(
            String token,
            Long userId,
            String fullName,
            String email,
            String role,
            String avatarUrl
            ) {

    }

    public record ForgotPasswordRequest(
            @NotBlank
            @Email(message = "Valid email is required") String email
            ) {

    }

    public record ResetPasswordRequest(
            @NotBlank(message = "Reset token is required") String token,
            @NotBlank
            @Size(min = 8, message = "Password must be at least 8 characters") String newPassword
            ) {

    }

    public record MessageResponse(String message) {

    }
}

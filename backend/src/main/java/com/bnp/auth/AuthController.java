package com.bnp.auth;

import com.bnp.auth.AuthDtos.AuthResponse;
import com.bnp.auth.AuthDtos.ForgotPasswordRequest;
import com.bnp.auth.AuthDtos.LoginRequest;
import com.bnp.auth.AuthDtos.MessageResponse;
import com.bnp.auth.AuthDtos.RegisterRequest;
import com.bnp.auth.AuthDtos.ResetPasswordRequest;
import com.bnp.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ApiResponse.ok("Account created", authService.register(req));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok("Logged in", authService.login(req));
    }

    @PostMapping("/forgot-password")
    public ApiResponse<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        String token = authService.forgotPassword(req);
        return ApiResponse.ok("Reset requested", new MessageResponse(token));
    }

    @PostMapping("/reset-password")
    public ApiResponse<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req);
        return ApiResponse.ok(new MessageResponse("Password has been reset successfully"));
    }
}

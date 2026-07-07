package com.bnp.auth;

import com.bnp.auth.AuthDtos.AuthResponse;
import com.bnp.auth.AuthDtos.ForgotPasswordRequest;
import com.bnp.auth.AuthDtos.LoginRequest;
import com.bnp.auth.AuthDtos.RegisterRequest;
import com.bnp.auth.AuthDtos.ResetPasswordRequest;
import com.bnp.common.enums.Enums.NotificationType;
import com.bnp.common.enums.Role;

import com.bnp.common.exception.BadRequestException;
import com.bnp.notification.NotificationService;
import com.bnp.security.JwtService;
import com.bnp.user.User;
import com.bnp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final com.bnp.security.CustomUserDetailsService userDetailsService;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationService notificationService;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new BadRequestException("An account with this email already exists");
        }

        String inviteCode = "BNP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        User user = User.builder()
                .fullName(req.fullName())
                .email(req.email().toLowerCase().trim())
                .password(passwordEncoder.encode(req.password()))
                .phone(req.phone())
                .profession(req.profession())
                .city(req.city())
                .state(req.state())
                .latitude(req.latitude())
                .longitude(req.longitude())
                .role(Role.FREE_USER)
                .inviteCode(inviteCode)
                .build();

        user = userRepository.save(user);

        // let the referrals module reward the inviter if an invite code was used
        eventPublisher.publishEvent(new UserRegisteredEvent(user.getId(), user.getEmail(), req.referralCode()));

        notificationService.notifyAdmins(NotificationType.SYSTEM,
                "New user registered",
                user.getFullName() + " just joined the platform.",
                "/pages/admin/users.html",
                user.getId());

        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email().toLowerCase().trim(), req.password()));

        User user = userRepository.findByEmail(req.email().toLowerCase().trim())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        return buildAuthResponse(user);
    }

    @Transactional
    public String forgotPassword(ForgotPasswordRequest req) {
        User user = userRepository.findByEmail(req.email().toLowerCase().trim()).orElse(null);
        if (user == null) {
            // don't reveal whether the email exists
            return "If an account exists for that email, a reset link has been sent.";
        }
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        // In production this token is emailed. Returned here so the flow is testable.
        return token;
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        User user = userRepository.findByResetToken(req.token())
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(req.newPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        UserDetails details = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(details);
        return new AuthResponse(token, user.getId(), user.getFullName(), user.getEmail(), user.getRole().name(), user.getAvatarUrl());
    }
}

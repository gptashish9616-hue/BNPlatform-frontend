package com.bnp.user;

import com.bnp.common.BaseEntity;
import com.bnp.common.enums.Enums.AccountStatus;
import com.bnp.common.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email", unique = true),
        @Index(name = "idx_users_city", columnList = "city"),
        @Index(name = "idx_users_state", columnList = "state")
})
public class User extends BaseEntity {

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.FREE_USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    // Profile fields
    private String profession;
    private String headline;
    @Column(columnDefinition = "TEXT")
    private String bio;
    private String avatarUrl;
    private String city;
    private String state;
    private Double latitude;
    private Double longitude;

    // Credibility
    @Builder.Default
    private Integer points = 0;

    @Builder.Default
    private Double avgRating = 0.0;

    @Builder.Default
    private Integer reviewCount = 0;

    // Free month reward tracking (from accepted app referrals)
    @Builder.Default
    private Integer freeMonthsEarned = 0;

    // Personal invite code — generated at registration, used for shareable referral links
    @Column(unique = true)
    private String inviteCode;

    // Password reset
    private String resetToken;
    private LocalDateTime resetTokenExpiry;

    @Builder.Default
    private Boolean emailVerified = false;

    public boolean isPremium() {
        return role == Role.PREMIUM_USER || role == Role.SUPER_ADMIN || role == Role.SUB_ADMIN;
    }
}

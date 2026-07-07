package com.bnp.auth;

/** Published when a new user registers, optionally carrying the invite/referral code they used. */
public record UserRegisteredEvent(Long userId, String email, String referralCode) {}

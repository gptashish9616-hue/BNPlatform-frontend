package com.bnp.common.enums;

/** The 4 platform roles. Stored on the User and mapped to Spring Security authorities. */
public enum Role {
    SUPER_ADMIN,
    SUB_ADMIN,
    PREMIUM_USER,
    FREE_USER
}

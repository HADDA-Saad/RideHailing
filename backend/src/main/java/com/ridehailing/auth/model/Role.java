package com.ridehailing.auth.model;

import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;

public enum Role {
    RIDER,
    DRIVER;

    public static Role fromAuthentication(Authentication authentication) {
        for (Role role : values()) {
            if (authentication.getAuthorities().stream()
                    .anyMatch(authority -> ("ROLE_" + role.name()).equals(authority.getAuthority()))) {
                return role;
            }
        }
        throw new AccessDeniedException("Rider or driver role required");
    }
}

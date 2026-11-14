package com.ridehailing.auth.security;

public record AuthenticatedUser(Long userId, String email) {
}

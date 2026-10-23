package com.ridehailing.auth.dto;

public record LoginResponse(String token, Long userId, String role) {}

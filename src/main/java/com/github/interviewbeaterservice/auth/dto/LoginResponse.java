package com.github.interviewbeaterservice.auth.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        Long userId,
        String email,
        String role
) {}

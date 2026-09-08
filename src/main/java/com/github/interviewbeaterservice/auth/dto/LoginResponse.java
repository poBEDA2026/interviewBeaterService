package com.github.interviewbeaterservice.auth.dto;

public record LoginResponse(
        String token,
        Long userId,
        String email,
        String role
) {}

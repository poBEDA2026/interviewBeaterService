package com.github.interviewbeaterservice.auth;

import java.util.Optional;

public final class AuthContext {

    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();

    private AuthContext() {}

    public static void set(Long userId) {
        CURRENT_USER_ID.set(userId);
    }

    public static Optional<Long> currentUserId() {
        return Optional.ofNullable(CURRENT_USER_ID.get());
    }

    static void clear() {
        CURRENT_USER_ID.remove();
    }
}

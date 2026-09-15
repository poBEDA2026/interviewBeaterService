package com.github.interviewbeaterservice.config;

/**
 * Константы для адреса и порта frontend-приложения.
 * <p>
 * Используются, в частности, в {@link SecurityConfig} для настройки CORS.
 * Значения должны совпадать с портом dev-сервера Vite (см. {@code frontend/vite.config.ts}).
 */
public final class FrontendConstants {

    /** Хост frontend-приложения (Vite dev-сервер). */
    public static final String FRONTEND_HOST = "localhost";

    /** Порт frontend-приложения (Vite dev-сервер). */
    public static final int FRONTEND_PORT = 5173;

    /** Полный origin frontend-приложения, включая протокол. */
    public static final String FRONTEND_ORIGIN = "http://" + FRONTEND_HOST + ":" + FRONTEND_PORT;

    private FrontendConstants() {
    }
}

package com.github.interviewbeaterservice.config;

/**
 * Константы для адреса и порта frontend-приложения.
 * <p>
 * Используются, в частности, в {@link SecurityConfig} для настройки CORS. Значения должны совпадать
 * с портом dev-сервера Vite (см. {@code frontend/vite.config.ts}).
 */
public final class FrontendConstants {

  public static final String FRONTEND_ORIGIN = "http://localhost:3000";

  private FrontendConstants() {
  }
}

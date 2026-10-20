package com.github.interviewbeaterservice.auth;

import java.util.Optional;

/**
 * Хранилище идентификатора текущего аутентифицированного пользователя в {@link ThreadLocal}.
 * Используется JWT-фильтром, чтобы пробросить userId в бизнес-слой без обращения к
 * SecurityContext.
 */
public final class AuthContext {

  private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();

  private AuthContext() {
  }

  /**
   * Установить идентификатор пользователя для текущего потока.
   *
   * @param userId ID пользователя
   */
  public static void set(Long userId) {
    CURRENT_USER_ID.set(userId);
  }

  /**
   * Получить идентификатор текущего пользователя.
   *
   * @return Optional с ID пользователя, либо пустой, если не задан
   */
  public static Optional<Long> currentUserId() {
    return Optional.ofNullable(CURRENT_USER_ID.get());
  }

  /**
   * Очистить {@link ThreadLocal} для текущего потока. Пакетный — вызывается из фильтра.
   */
  static void clear() {
    CURRENT_USER_ID.remove();
  }
}

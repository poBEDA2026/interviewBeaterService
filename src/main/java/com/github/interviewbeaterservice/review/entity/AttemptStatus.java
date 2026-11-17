package com.github.interviewbeaterservice.review.entity;

/**
 * Статус попытки ревью: в процессе прохождения либо уже сабмитнута.
 */
public enum AttemptStatus {
  /**
   * Попытка в процессе прохождения: пользователь отвечает на вопросы
   */
  IN_PROGRESS,

  /**
   * Попытка завершена и сабмитнута: ответы зафиксированы, результат подсчитан
   */
  SUBMITTED
}
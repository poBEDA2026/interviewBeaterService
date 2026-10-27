package com.github.interviewbeaterservice.review.exception;

/**
 * Исключение, выбрасываемое при создании подборки с некорректным набором вопросов
 * (пустой заголовок, отсутствие вопросов или повторяющиеся идентификаторы вопросов).
 */
public class ReviewSetValidationException extends RuntimeException {

  /**
   * Создание исключения с произвольным сообщением
   *
   * @param message описание причины отказа
   */
  public ReviewSetValidationException(String message) {
    super(message);
  }
}
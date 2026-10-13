package com.github.interviewbeaterservice.question.exception;

public class InvalidAttachmentException extends RuntimeException {

  /**
   * Создаёт исключение с описанием причины невалидности вложения.
   *
   * @param message описание ошибки
   */
  public InvalidAttachmentException(String message) {
    super(message);
  }
}

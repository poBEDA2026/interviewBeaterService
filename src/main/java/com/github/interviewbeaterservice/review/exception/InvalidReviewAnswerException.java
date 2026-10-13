package com.github.interviewbeaterservice.review.exception;

/**
 * Исключение, выбрасываемое при попытке сохранить ответ, не удовлетворяющий правилам попытки:
 * вопрос не входит в подборку, вариант ответа не принадлежит вопросу или сам ответ не найден.
 */
public class InvalidReviewAnswerException extends RuntimeException {

  /**
   * Создание исключения с произвольным сообщением
   *
   * @param message описание причины отказа
   */
  public InvalidReviewAnswerException(String message) {
    super(message);
  }
}

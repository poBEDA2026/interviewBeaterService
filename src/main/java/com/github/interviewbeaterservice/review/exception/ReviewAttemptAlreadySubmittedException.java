package com.github.interviewbeaterservice.review.exception;

/**
 * Исключение, выбрасываемое при попытке изменить уже сабмитнутую попытку ревью
 * (например, сохранить новый ответ после {@code submit}).
 */
public class ReviewAttemptAlreadySubmittedException extends RuntimeException {

  /**
   * Создание исключения для попытки с указанным идентификатором
   *
   * @param id идентификатор {@link com.github.interviewbeaterservice.review.entity.ReviewAttempt}
   */
  public ReviewAttemptAlreadySubmittedException(Long id) {
    super("Review attempt " + id + " is already submitted");
  }
}

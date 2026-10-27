package com.github.interviewbeaterservice.review.exception;

/**
 * Исключение, выбрасываемое при обращении к несуществующей попытке ревью
 * либо к попытке, принадлежащей другому пользователю.
 */
public class ReviewAttemptNotFoundException extends RuntimeException {

  /**
   * Создание исключения для попытки с указанным идентификатором
   *
   * @param id идентификатор {@link com.github.interviewbeaterservice.review.entity.ReviewAttempt}
   */
  public ReviewAttemptNotFoundException(Long id) {
    super("Review attempt " + id + " not found");
  }
}
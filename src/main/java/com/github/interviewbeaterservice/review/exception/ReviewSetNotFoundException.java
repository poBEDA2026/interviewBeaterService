package com.github.interviewbeaterservice.review.exception;

/**
 * Исключение, выбрасываемое при обращении к несуществующей подборке вопросов для ревью.
 */
public class ReviewSetNotFoundException extends RuntimeException {

  /**
   * Создание исключения для подборки с указанным идентификатором
   *
   * @param id идентификатор {@link com.github.interviewbeaterservice.review.entity.ReviewSet}
   */
  public ReviewSetNotFoundException(Long id) {
    super("Review set " + id + " not found");
  }
}
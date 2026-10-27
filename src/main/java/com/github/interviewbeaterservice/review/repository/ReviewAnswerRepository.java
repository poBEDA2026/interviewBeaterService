package com.github.interviewbeaterservice.review.repository;

import com.github.interviewbeaterservice.review.entity.ReviewAnswer;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA-репозиторий для доступа к ответам пользователей внутри попыток ревью {@link ReviewAnswer}.
 */
public interface ReviewAnswerRepository extends JpaRepository<ReviewAnswer, Long> {

  /**
   * Получение ответа на конкретный вопрос внутри указанной попытки
   *
   * @param attemptId идентификатор попытки
   * @param questionId идентификатор вопроса
   * @return {@link Optional} с найденным ответом или пусто
   */
  Optional<ReviewAnswer> findByAttemptIdAndQuestionId(Long attemptId, Long questionId);

  /**
   * Получение всех ответов внутри указанной попытки
   *
   * @param attemptId идентификатор попытки
   * @return список {@link ReviewAnswer}
   */
  List<ReviewAnswer> findAllByAttemptId(Long attemptId);
}
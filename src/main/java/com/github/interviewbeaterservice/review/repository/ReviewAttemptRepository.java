package com.github.interviewbeaterservice.review.repository;

import com.github.interviewbeaterservice.review.entity.AttemptStatus;
import com.github.interviewbeaterservice.review.entity.ReviewAttempt;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA-репозиторий для доступа к попыткам прохождения подборок {@link ReviewAttempt}.
 */
public interface ReviewAttemptRepository extends JpaRepository<ReviewAttempt, Long> {

  /**
   * Получение первой попытки пользователя по подборке с указанным статусом
   *
   * @param userId идентификатор пользователя
   * @param setId идентификатор подборки
   * @param status статус {@link AttemptStatus}
   * @return {@link Optional} с найденной попыткой или пусто
   */
  Optional<ReviewAttempt> findFirstByUserIdAndSetIdAndStatus(
      Long userId, Long setId, AttemptStatus status);

  /**
   * Получение последней попытки пользователя по подборке с указанным статусом,
   * сабмитнутой не позже указанного момента
   *
   * @param userId идентификатор пользователя
   * @param setId идентификатор подборки
   * @param status статус {@link AttemptStatus}
   * @param before верхняя граница времени сабмита
   * @return {@link Optional} с найденной попыткой или пусто
   */
  Optional<ReviewAttempt> findFirstByUserIdAndSetIdAndStatusAndSubmittedAtBeforeOrderBySubmittedAtDesc(
      Long userId, Long setId, AttemptStatus status, Instant before);

  /**
   * Получение всех попыток пользователя по подборке с указанным статусом,
   * отсортированных по возрастанию времени сабмита
   *
   * @param userId идентификатор пользователя
   * @param setId идентификатор подборки
   * @param status статус {@link AttemptStatus}
   * @return список {@link ReviewAttempt}, упорядоченный по {@code submittedAt}
   */
  List<ReviewAttempt> findAllByUserIdAndSetIdAndStatusOrderBySubmittedAtAsc(
      Long userId, Long setId, AttemptStatus status);

  /**
   * Получение всех попыток пользователя с указанным статусом,
   * отсортированных по убыванию времени сабмита
   *
   * @param userId идентификатор пользователя
   * @param status статус {@link AttemptStatus}
   * @return список {@link ReviewAttempt}, упорядоченный по убыванию {@code submittedAt}
   */
  List<ReviewAttempt> findAllByUserIdAndStatusOrderBySubmittedAtDesc(
      Long userId, AttemptStatus status);
}
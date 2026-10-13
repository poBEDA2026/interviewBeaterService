package com.github.interviewbeaterservice.review.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.interviewbeaterservice.review.entity.AttemptStatus;
import com.github.interviewbeaterservice.review.entity.ReviewAttempt;
import com.github.interviewbeaterservice.review.entity.ReviewSet;
import com.github.interviewbeaterservice.support.AbstractPostgresTest;
import com.github.interviewbeaterservice.user.entity.User;
import com.github.interviewbeaterservice.user.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

/**
 * Интеграционные тесты репозитория {@link ReviewAttemptRepository} на реальном
 * PostgreSQL: проверка корректности сгенерированных finder-запросов.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReviewAttemptRepositoryTest extends AbstractPostgresTest {

  @Autowired
  TestEntityManager em;
  @Autowired
  ReviewAttemptRepository repo;
  @Autowired
  UserRepository userRepository;

  @Test
  @DisplayName("Поиск незавершённой попытки возвращает существующую попытку пользователя")
  void findInProgressReturnsExisting() {
    User u = persistUser("u@x.com");
    ReviewSet s = persistSet("Set");
    ReviewAttempt a = persistAttempt(u, s, AttemptStatus.IN_PROGRESS);

    Optional<ReviewAttempt> found = repo
        .findFirstByUserIdAndSetIdAndStatus(u.getId(), s.getId(), AttemptStatus.IN_PROGRESS);

    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(a.getId());
  }

  @Test
  @DisplayName("Поиск предыдущей попытки возвращает сабмитнутую попытку с наибольшим временем сабмита")
  void findPreviousReturnsLatestSubmitted() {
    User u = persistUser("u@x.com");
    ReviewSet s = persistSet("Set");
    ReviewAttempt older = persistSubmitted(u, s, Instant.now().minusSeconds(100));
    ReviewAttempt newer = persistSubmitted(u, s, Instant.now().minusSeconds(10));

    Instant now = Instant.now();
    Optional<ReviewAttempt> found = repo
        .findFirstByUserIdAndSetIdAndStatusAndSubmittedAtBeforeOrderBySubmittedAtDesc(
            u.getId(), s.getId(), AttemptStatus.SUBMITTED, now);

    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(newer.getId());
  }

  @Test
  @DisplayName("Поиск предыдущей попытки игнорирует незавершённые попытки")
  void findPreviousIgnoresInProgress() {
    User u = persistUser("u@x.com");
    ReviewSet s = persistSet("Set");
    persistAttempt(u, s, AttemptStatus.IN_PROGRESS);

    Optional<ReviewAttempt> found = repo
        .findFirstByUserIdAndSetIdAndStatusAndSubmittedAtBeforeOrderBySubmittedAtDesc(
            u.getId(), s.getId(), AttemptStatus.SUBMITTED, Instant.now());

    assertThat(found).isEmpty();
  }

  private User persistUser(String email) {
    User u = User.builder().email(email).password("x").build();
    return userRepository.saveAndFlush(u);
  }

  private ReviewSet persistSet(String title) {
    ReviewSet s = ReviewSet.builder().title(title).build();
    em.persistAndFlush(s);
    return s;
  }

  private ReviewAttempt persistAttempt(User u, ReviewSet s, AttemptStatus status) {
    ReviewAttempt a = ReviewAttempt.builder()
        .user(u).set(s).totalQuestions(3).build();
    a.setStatus(status);
    em.persistAndFlush(a);
    return a;
  }

  private ReviewAttempt persistSubmitted(User u, ReviewSet s, Instant submittedAt) {
    ReviewAttempt a = ReviewAttempt.builder()
        .user(u).set(s).totalQuestions(3).build();
    a.setStatus(AttemptStatus.SUBMITTED);
    a.setSubmittedAt(submittedAt);
    a.setCorrectCount(2);
    em.persistAndFlush(a);
    return a;
  }
}
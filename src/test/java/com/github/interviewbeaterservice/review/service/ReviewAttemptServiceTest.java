package com.github.interviewbeaterservice.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.interviewbeaterservice.answer.entity.Answer;
import com.github.interviewbeaterservice.answer.repository.AnswerRepository;
import com.github.interviewbeaterservice.question.entity.Question;
import com.github.interviewbeaterservice.review.entity.AttemptStatus;
import com.github.interviewbeaterservice.review.entity.ReviewAnswer;
import com.github.interviewbeaterservice.review.entity.ReviewAttempt;
import com.github.interviewbeaterservice.review.entity.ReviewSet;
import com.github.interviewbeaterservice.review.entity.ReviewSetItem;
import com.github.interviewbeaterservice.review.exception.InvalidReviewAnswerException;
import com.github.interviewbeaterservice.review.exception.ReviewAttemptAlreadySubmittedException;
import com.github.interviewbeaterservice.review.exception.ReviewAttemptNotFoundException;
import com.github.interviewbeaterservice.review.repository.ReviewAnswerRepository;
import com.github.interviewbeaterservice.review.repository.ReviewAttemptRepository;
import com.github.interviewbeaterservice.review.repository.ReviewSetItemRepository;
import com.github.interviewbeaterservice.user.entity.User;
import com.github.interviewbeaterservice.user.repository.UserRepository;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Юнит-тесты сервиса {@link ReviewAttemptService}: жизненный цикл попытки прохождения
 * подборки — старт, сохранение ответов, подсчёт результатов при сабмите и чтение состояния.
 */
@ExtendWith(MockitoExtension.class)
class ReviewAttemptServiceTest {

  @Mock
  ReviewAttemptRepository attemptRepository;
  @Mock
  ReviewSetItemRepository itemRepository;
  @Mock
  ReviewAnswerRepository answerRepository;
  @Mock
  UserRepository userRepository;
  @Mock
  ReviewSetService reviewSetService;
  @Mock
  AnswerRepository coreAnswerRepository;

  ReviewAttemptService service;

  private final User alice = newUser(1L);
  private final ReviewSet set = newSet(7L, 3);

  @BeforeEach
  void setUp() {
    service = new ReviewAttemptService(attemptRepository, itemRepository, answerRepository,
        userRepository, reviewSetService, coreAnswerRepository);
  }

  @Test
  @DisplayName("Старт возвращает уже существующую незавершённую попытку без создания новой")
  void startAttemptReturnsExistingInProgress() {
    ReviewAttempt existing = ReviewAttempt.builder().user(alice).set(set).totalQuestions(3).build();
    setId(existing, 42L);
    when(attemptRepository.findFirstByUserIdAndSetIdAndStatus(1L, 7L, AttemptStatus.IN_PROGRESS))
        .thenReturn(Optional.of(existing));

    ReviewAttempt result = service.startAttempt(1L, 7L);

    assertThat(result).isSameAs(existing);
    verify(attemptRepository, never()).save(any());
  }

  @Test
  @DisplayName("Старт без незавершённой попытки создаёт новую попытку в статусе IN_PROGRESS")
  void startAttemptCreatesNewWhenNoneInProgress() {
    when(attemptRepository.findFirstByUserIdAndSetIdAndStatus(1L, 7L, AttemptStatus.IN_PROGRESS))
        .thenReturn(Optional.empty());
    when(reviewSetService.getById(7L)).thenReturn(set);
    when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
    when(attemptRepository.save(any(ReviewAttempt.class))).thenAnswer(inv -> {
      ReviewAttempt a = inv.getArgument(0);
      setId(a, 99L);
      return a;
    });

    ReviewAttempt result = service.startAttempt(1L, 7L);

    assertThat(result.getId()).isEqualTo(99L);
    assertThat(result.getStatus()).isEqualTo(AttemptStatus.IN_PROGRESS);
    assertThat(result.getTotalQuestions()).isEqualTo(3);
  }

  @Test
  @DisplayName("Старт попытки для несуществующей подборки выбрасывает ReviewSetNotFoundException")
  void startAttemptThrowsWhenSetMissing() {
    when(attemptRepository.findFirstByUserIdAndSetIdAndStatus(1L, 7L, AttemptStatus.IN_PROGRESS))
        .thenReturn(Optional.empty());
    when(reviewSetService.getById(7L)).thenThrow(
        new com.github.interviewbeaterservice.review.exception.ReviewSetNotFoundException(7L));

    assertThatThrownBy(() -> service.startAttempt(1L, 7L))
        .isInstanceOf(
            com.github.interviewbeaterservice.review.exception.ReviewSetNotFoundException.class);
    verify(attemptRepository, never()).save(any());
  }

  @Test
  @DisplayName("Сохранение ответа для несуществующей попытки выбрасывает ReviewAttemptNotFoundException")
  void saveAnswerThrowsWhenAttemptMissing() {
    when(attemptRepository.findById(42L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.saveAnswer(1L, 42L, 10L, 20L))
        .isInstanceOf(ReviewAttemptNotFoundException.class);
  }

  @Test
  @DisplayName("Сохранение ответа в попытку другого пользователя выбрасывает ReviewAttemptNotFoundException")
  void saveAnswerThrowsWhenForeignAttempt() {
    ReviewAttempt a = ReviewAttempt.builder().user(newUser(99L)).set(set).totalQuestions(3).build();
    setId(a, 42L);
    when(attemptRepository.findById(42L)).thenReturn(Optional.of(a));
    assertThatThrownBy(() -> service.saveAnswer(1L, 42L, 10L, 20L))
        .isInstanceOf(ReviewAttemptNotFoundException.class);
  }

  @Test
  @DisplayName("Сохранение ответа в уже завершённую попытку выбрасывает ReviewAttemptAlreadySubmittedException")
  void saveAnswerThrowsWhenAlreadySubmitted() {
    ReviewAttempt a = ReviewAttempt.builder().user(alice).set(set).totalQuestions(3).build();
    a.setStatus(AttemptStatus.SUBMITTED);
    setId(a, 42L);
    when(attemptRepository.findById(42L)).thenReturn(Optional.of(a));
    assertThatThrownBy(() -> service.saveAnswer(1L, 42L, 10L, 20L))
        .isInstanceOf(ReviewAttemptAlreadySubmittedException.class);
  }

  @Test
  @DisplayName("Сохранение ответа на вопрос, не входящий в подборку, выбрасывает InvalidReviewAnswerException")
  void saveAnswerThrowsWhenQuestionNotInSet() {
    ReviewAttempt a = ReviewAttempt.builder().user(alice).set(set).totalQuestions(3).build();
    setId(a, 42L);
    when(attemptRepository.findById(42L)).thenReturn(Optional.of(a));
    when(itemRepository.findAllBySetIdOrderByPositionAsc(7L)).thenReturn(set.getItems());

    assertThatThrownBy(() -> service.saveAnswer(1L, 42L, 999L, 20L))
        .isInstanceOf(InvalidReviewAnswerException.class);
  }

  @Test
  @DisplayName("Сохранение ответа сохраняет ответ и оставляет признак корректности неопределённым до сабмита")
  void saveAnswerPersistsAndLeavesIsCorrectNull() {
    ReviewAttempt a = ReviewAttempt.builder().user(alice).set(set).totalQuestions(3).build();
    setId(a, 42L);
    when(attemptRepository.findById(42L)).thenReturn(Optional.of(a));
    when(itemRepository.findAllBySetIdOrderByPositionAsc(7L)).thenReturn(set.getItems());

    Question q = set.getItems().get(0).getQuestion();
    setId(q, 10L);
    Answer ans = new Answer();
    setId(ans, 20L);
    ans.setQuestion(q);
    when(coreAnswerRepository.findById(20L)).thenReturn(Optional.of(ans));
    when(answerRepository.findByAttemptIdAndQuestionId(42L, 10L)).thenReturn(Optional.empty());
    when(answerRepository.save(any(ReviewAnswer.class))).thenAnswer(inv -> inv.getArgument(0));

    ReviewAnswer result = service.saveAnswer(1L, 42L, 10L, 20L);

    assertThat(result.getIsCorrect()).isNull();
    verify(answerRepository).save(any(ReviewAnswer.class));
  }

  @Test
  @DisplayName("Сабмит без сохранённых ответов считает все вопросы неверными и проставляет время завершения")
  void submitEmptyAnswersMarksAllWrong() {
    ReviewAttempt a = ReviewAttempt.builder().user(alice).set(set).totalQuestions(3).build();
    setId(a, 42L);
    Question q0 = set.getItems().get(0).getQuestion();
    Question q1 = set.getItems().get(1).getQuestion();
    Question q2 = set.getItems().get(2).getQuestion();
    setId(q0, 100L);
    setId(q1, 101L);
    setId(q2, 102L);
    Answer correct100 = newAnswer(201L, true);
    Answer correct101 = newAnswer(202L, true);
    Answer correct102 = newAnswer(203L, true);
    correct100.setQuestion(q0);
    correct101.setQuestion(q1);
    correct102.setQuestion(q2);
    when(attemptRepository.findById(42L)).thenReturn(Optional.of(a));
    when(answerRepository.findAllByAttemptId(42L)).thenReturn(List.of());
    when(itemRepository.findAllBySetIdOrderByPositionAsc(7L)).thenReturn(set.getItems());
    when(coreAnswerRepository.findFirstByQuestionIdAndIsCorrectTrue(100L)).thenReturn(
        Optional.of(correct100));
    when(coreAnswerRepository.findFirstByQuestionIdAndIsCorrectTrue(101L)).thenReturn(
        Optional.of(correct101));
    when(coreAnswerRepository.findFirstByQuestionIdAndIsCorrectTrue(102L)).thenReturn(
        Optional.of(correct102));
    when(attemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    ReviewAttemptService.SubmitResult result = service.submit(1L, 42L);

    assertThat(result.correctCount()).isZero();
    assertThat(result.percentage()).isEqualTo(0.0);
    assertThat(a.getStatus()).isEqualTo(AttemptStatus.SUBMITTED);
    assertThat(a.getCorrectCount()).isZero();
    assertThat(a.getSubmittedAt()).isNotNull();
    verify(answerRepository, times(3)).save(any(ReviewAnswer.class));
  }

  @Test
  @DisplayName("Сабмит считает верные ответы и вычисляет процент правильных")
  void submitCountsCorrectAnswers() {
    ReviewAttempt a = ReviewAttempt.builder().user(alice).set(set).totalQuestions(3).build();
    setId(a, 42L);
    when(attemptRepository.findById(42L)).thenReturn(Optional.of(a));
    Question q0 = set.getItems().get(0).getQuestion();
    Question q1 = set.getItems().get(1).getQuestion();
    Question q2 = set.getItems().get(2).getQuestion();
    setId(q0, 100L);
    setId(q1, 101L);
    setId(q2, 102L);
    Answer ans1 = newAnswer(1L, true);
    Answer ans2 = newAnswer(2L, true);
    Answer ans3 = newAnswer(3L, false);
    ReviewAnswer ra1 = ReviewAnswer.builder()
        .attempt(a).question(q0).answer(ans1).build();
    ReviewAnswer ra2 = ReviewAnswer.builder()
        .attempt(a).question(q1).answer(ans2).build();
    ReviewAnswer ra3 = ReviewAnswer.builder()
        .attempt(a).question(q2).answer(ans3).build();
    when(answerRepository.findAllByAttemptId(42L)).thenReturn(List.of(ra1, ra2, ra3));
    when(itemRepository.findAllBySetIdOrderByPositionAsc(7L)).thenReturn(set.getItems());
    when(
        attemptRepository.findFirstByUserIdAndSetIdAndStatusAndSubmittedAtBeforeOrderBySubmittedAtDesc(
            any(), any(), any(), any())).thenReturn(Optional.empty());
    when(attemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    ReviewAttemptService.SubmitResult result = service.submit(1L, 42L);

    assertThat(result.correctCount()).isEqualTo(2);
    assertThat(result.percentage()).isEqualTo(66.67);
    assertThat(ra1.getIsCorrect()).isTrue();
    assertThat(ra2.getIsCorrect()).isTrue();
    assertThat(ra3.getIsCorrect()).isFalse();
  }

  @Test
  @DisplayName("Повторный сабмит завершённой попытки возвращает прежний результат без пересчёта")
  void submitIsIdempotentForSubmittedAttempt() {
    ReviewAttempt a = ReviewAttempt.builder().user(alice).set(set).totalQuestions(3).build();
    a.setStatus(AttemptStatus.SUBMITTED);
    a.setCorrectCount(2);
    a.setSubmittedAt(java.time.Instant.now().minusSeconds(100));
    setId(a, 42L);
    when(attemptRepository.findById(42L)).thenReturn(Optional.of(a));
    when(
        attemptRepository.findFirstByUserIdAndSetIdAndStatusAndSubmittedAtBeforeOrderBySubmittedAtDesc(
            any(), any(), any(), any())).thenReturn(Optional.empty());

    ReviewAttemptService.SubmitResult result = service.submit(1L, 42L);

    assertThat(result.correctCount()).isEqualTo(2);
    verify(answerRepository, never()).save(any());
    verify(attemptRepository, never()).save(any());
  }

  @Test
  @DisplayName("Результат сабмита содержит предыдущую попытку по той же подборке")
  void submitReturnsPreviousAttempt() {
    ReviewAttempt a = ReviewAttempt.builder().user(alice).set(set).totalQuestions(3).build();
    setId(a, 42L);
    ReviewAttempt prev = ReviewAttempt.builder().user(alice).set(set).totalQuestions(3).build();
    prev.setStatus(AttemptStatus.SUBMITTED);
    prev.setCorrectCount(1);
    prev.setSubmittedAt(java.time.Instant.now().minusSeconds(1000));
    setId(prev, 35L);
    Question q0 = set.getItems().get(0).getQuestion();
    Question q1 = set.getItems().get(1).getQuestion();
    Question q2 = set.getItems().get(2).getQuestion();
    setId(q0, 100L);
    setId(q1, 101L);
    setId(q2, 102L);
    Answer sentinel = newAnswer(999L, true);
    when(attemptRepository.findById(42L)).thenReturn(Optional.of(a));
    when(answerRepository.findAllByAttemptId(42L)).thenReturn(List.of());
    when(itemRepository.findAllBySetIdOrderByPositionAsc(7L)).thenReturn(set.getItems());
    when(coreAnswerRepository.findFirstByQuestionIdAndIsCorrectTrue(any())).thenReturn(
        Optional.of(sentinel));
    when(
        attemptRepository.findFirstByUserIdAndSetIdAndStatusAndSubmittedAtBeforeOrderBySubmittedAtDesc(
            eq(1L), eq(7L), eq(AttemptStatus.SUBMITTED), any())).thenReturn(Optional.of(prev));
    when(attemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    ReviewAttemptService.SubmitResult result = service.submit(1L, 42L);

    assertThat(result.previous()).isPresent();
    assertThat(result.previous().get().getId()).isEqualTo(35L);
    assertThat(result.previous().get().getCorrectCount()).isEqualTo(1);
  }

  @Test
  @DisplayName("Сабмит попытки другого пользователя выбрасывает ReviewAttemptNotFoundException")
  void submitThrowsOnForeignAttempt() {
    ReviewAttempt a = ReviewAttempt.builder().user(newUser(99L)).set(set).totalQuestions(3).build();
    setId(a, 42L);
    when(attemptRepository.findById(42L)).thenReturn(Optional.of(a));
    assertThatThrownBy(() -> service.submit(1L, 42L))
        .isInstanceOf(ReviewAttemptNotFoundException.class);
  }

  @Test
  @DisplayName("Чтение состояния возвращает попытку текущего пользователя")
  void getStateReturnsAttempt() {
    ReviewAttempt a = ReviewAttempt.builder().user(alice).set(set).totalQuestions(3).build();
    setId(a, 42L);
    when(attemptRepository.findById(42L)).thenReturn(Optional.of(a));
    assertThat(service.getState(1L, 42L)).isSameAs(a);
  }

  @Test
  @DisplayName("Чтение состояния попытки другого пользователя выбрасывает ReviewAttemptNotFoundException")
  void getStateThrowsOnForeign() {
    ReviewAttempt a = ReviewAttempt.builder().user(newUser(99L)).set(set).totalQuestions(3).build();
    setId(a, 42L);
    when(attemptRepository.findById(42L)).thenReturn(Optional.of(a));
    assertThatThrownBy(() -> service.getState(1L, 42L))
        .isInstanceOf(ReviewAttemptNotFoundException.class);
  }

  @Test
  @DisplayName("Чтение результата незавершённой попытки выбрасывает IllegalStateException")
  void getResultThrowsIfNotSubmitted() {
    ReviewAttempt a = ReviewAttempt.builder().user(alice).set(set).totalQuestions(3).build();
    setId(a, 42L);
    when(attemptRepository.findById(42L)).thenReturn(Optional.of(a));
    assertThatThrownBy(() -> service.getResult(1L, 42L))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  @DisplayName("Чтение результата завершённой попытки возвращает саму попытку")
  void getResultReturnsSubmitted() {
    ReviewAttempt a = ReviewAttempt.builder().user(alice).set(set).totalQuestions(3).build();
    a.setStatus(AttemptStatus.SUBMITTED);
    a.setCorrectCount(2);
    a.setSubmittedAt(java.time.Instant.now());
    setId(a, 42L);
    when(attemptRepository.findById(42L)).thenReturn(Optional.of(a));
    assertThat(service.getResult(1L, 42L)).isSameAs(a);
  }

  private static Answer newAnswer(Long id, boolean correct) {
    Answer a = new Answer();
    setId(a, id);
    a.setCorrect(correct);
    return a;
  }

  private static User newUser(Long id) {
    User u = User.builder().email("u@x").password("p").build();
    setId(u, id);
    return u;
  }

  private static ReviewSet newSet(Long id, int itemCount) {
    ReviewSet s = ReviewSet.builder().title("S").build();
    setId(s, id);
    List<ReviewSetItem> items = new ArrayList<>();
    for (int i = 0; i < itemCount; i++) {
      Question q = new Question();
      setId(q, 100L + i);
      ReviewSetItem item = ReviewSetItem.builder().set(s).question(q).position(i).build();
      items.add(item);
    }
    s.getItems().addAll(items);
    return s;
  }

  private static void setId(Object target, Long id) {
    try {
      Field f = target.getClass().getDeclaredField("id");
      f.setAccessible(true);
      f.set(target, id);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}

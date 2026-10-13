package com.github.interviewbeaterservice.review.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.interviewbeaterservice.answer.repository.AnswerRepository;
import com.github.interviewbeaterservice.auth.AuthContext;
import com.github.interviewbeaterservice.auth.JwtAuthFilter;
import com.github.interviewbeaterservice.question.repository.QuestionRepository;
import com.github.interviewbeaterservice.review.entity.AttemptStatus;
import com.github.interviewbeaterservice.review.entity.ReviewAttempt;
import com.github.interviewbeaterservice.review.entity.ReviewSet;
import com.github.interviewbeaterservice.review.exception.ReviewAttemptNotFoundException;
import com.github.interviewbeaterservice.review.repository.ReviewAnswerRepository;
import com.github.interviewbeaterservice.review.repository.ReviewSetItemRepository;
import com.github.interviewbeaterservice.review.service.ReviewAttemptService;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Тесты HTTP-слоя контроллера {@link ReviewAttemptController}: разбор контекста
 * авторизации, сериализация ответов и маппинг доменных исключений в коды ответа.
 */
@WebMvcTest(controllers = ReviewAttemptController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class))
class ReviewAttemptControllerTest {

  @Autowired
  MockMvc mockMvc;
  @MockitoBean
  ReviewAttemptService service;
  @MockitoBean
  ReviewSetItemRepository itemRepository;
  @MockitoBean
  ReviewAnswerRepository answerRepository;
  @MockitoBean
  QuestionRepository questionRepository;
  @MockitoBean
  AnswerRepository answerRepository2;

  @AfterEach
  void clear() {
    AuthContext.clear();
  }

  @Test
  @DisplayName("Старт попытки возвращает 200 с идентификатором, статусом и числом вопросов")
  void startAttemptReturns200() throws Exception {
    AuthContext.set(1L);
    when(service.startAttempt(1L, 7L)).thenReturn(attemptWithId(42L, 5));
    mockMvc.perform(post("/review-sets/7/attempts"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.attemptId").value(42))
        .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
        .andExpect(jsonPath("$.totalQuestions").value(5));
  }

  @Test
  @DisplayName("Старт попытки без пользователя в контексте авторизации возвращает 401")
  void startAttemptReturns401WithoutUser() throws Exception {
    // AuthContext пуст
    mockMvc.perform(post("/review-sets/7/attempts"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Сохранение ответа для несуществующей попытки возвращает 404")
  void saveAnswerReturns404WhenAttemptMissing() throws Exception {
    AuthContext.set(1L);
    when(service.saveAnswer(any(), any(), any(), any()))
        .thenThrow(new ReviewAttemptNotFoundException(42L));
    String body = new ObjectMapper().writeValueAsString(Map.of("questionId", 1, "answerId", 2));
    mockMvc.perform(post("/api/attempts/42/answers")
            .contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Сохранение ответа с пустыми обязательными полями возвращает 400")
  void saveAnswerValidationError400() throws Exception {
    AuthContext.set(1L);
    String body = """
        {"questionId":null,"answerId":null}
        """;
    mockMvc.perform(post("/attempts/42/answers")
            .contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Сабмит попытки возвращает 200 с числом верных ответов и процентом")
  void submitReturnsResult() throws Exception {
    AuthContext.set(1L);
    ReviewAttempt a = attemptWithId(42L, 5);
    a.setStatus(AttemptStatus.SUBMITTED);
    a.setCorrectCount(3);
    a.setSubmittedAt(java.time.Instant.now());
    when(service.submit(1L, 42L)).thenReturn(
        new ReviewAttemptService.SubmitResult(a, Optional.empty(), 3, 60.0));
    mockMvc.perform(post("/attempts/42/submit"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.correctCount").value(3))
        .andExpect(jsonPath("$.percentage").value(60.0));
  }

  @Test
  @DisplayName("Сабмит попытки другого пользователя возвращает 404")
  void submitOnForeignReturns404() throws Exception {
    AuthContext.set(1L);
    when(service.submit(1L, 42L))
        .thenThrow(new ReviewAttemptNotFoundException(42L));
    mockMvc.perform(post("/attempts/42/submit"))
        .andExpect(status().isNotFound());
  }

  private static ReviewAttempt attemptWithId(Long id, int total) {
    ReviewSet s = ReviewSet.builder().title("S").build();
    ReviewAttempt a = ReviewAttempt.builder()
        .user(new com.github.interviewbeaterservice.user.entity.User())
        .set(s).totalQuestions(total).build();
    setId(a, id);
    return a;
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

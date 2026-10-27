package com.github.interviewbeaterservice.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.interviewbeaterservice.auth.exception.BadCredentialsException;
import com.github.interviewbeaterservice.review.exception.InvalidReviewAnswerException;
import com.github.interviewbeaterservice.review.exception.ReviewAttemptNotFoundException;
import com.github.interviewbeaterservice.review.exception.ReviewSetNotFoundException;
import com.github.interviewbeaterservice.review.exception.ReviewSetValidationException;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ControllerAdviceTest {

  private final ControllerAdvice advice = new ControllerAdvice();

  @Test
  @DisplayName("Возвращается 401 ошибка при логине с неверными реквизитами")
  void badCredentials_returns401() {
    ResponseEntity<Map<String, String>> response = advice.handleBadCredentials(
        new BadCredentialsException());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getBody()).containsEntry("error", "Invalid email or password");
  }

  @Test
  @DisplayName("Возвращается 409 ошибка при регистрации пользователя с уже зарегистрированной почтой")
  void dataIntegrityViolation_returns409() {
    ResponseEntity<Map<String, String>> response = advice.handleDataIntegrity(
        new DataIntegrityViolationException("dup"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody()).containsEntry("error", "Email already registered");
  }

  @Test
  void reviewSetNotFound_returns404() {
    ResponseEntity<Map<String, String>> response = advice.handleReviewSetNotFound(
        new ReviewSetNotFoundException(7L));
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).containsEntry("error", "Review set 7 not found");
  }

  @Test
  void reviewAttemptNotFound_returns404() {
    ResponseEntity<Map<String, String>> response = advice.handleReviewAttemptNotFound(
        new ReviewAttemptNotFoundException(42L));
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void invalidReviewAnswer_returns400() {
    ResponseEntity<Map<String, String>> response = advice.handleInvalidReviewAnswer(
        new InvalidReviewAnswerException("bad"));
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).containsEntry("error", "bad");
  }

  @Test
  void reviewSetValidation_returns400() {
    ResponseEntity<Map<String, String>> response = advice.handleReviewSetValidation(
        new ReviewSetValidationException("empty"));
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }
}

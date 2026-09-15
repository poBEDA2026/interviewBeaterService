package com.github.interviewbeaterservice.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.security.sasl.AuthenticationException;
import java.util.Map;

@RestControllerAdvice
public class AuthExceptionHandler {

  /**
   * Обработка {@link BadCredentialsException}
   *
   * @param ex исключение
   * @return ответ для фронта с соответствующим кодом ошибки и сообщением
   */
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(Map.of("error", "Bad credentials"));
  }

  /**
   * Обработка {@link AuthenticationException}
   *
   * @param ex исключение
   * @return ответ для фронта с соответствующим кодом ошибки и сообщением
   */
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<Map<String, String>> handleIllegalArgument(AuthenticationException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(Map.of("error", "Bad credentials"));
  }
}

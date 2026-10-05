package com.github.interviewbeaterservice.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ControllerAdviceTest {

    private final ControllerAdvice advice = new ControllerAdvice();

    @Test
    @DisplayName("Возвращается 401 ошибка при логине с неверными реквизитами")
    void badCredentials_returns401() {
        ResponseEntity<Map<String, String>> response = advice.handleBadCredentials(new BadCredentialsException("Invalid email or password"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("error", "Invalid email or password");
    }

    @Test
    @DisplayName("Возвращается 409 ошибка при регистрации пользователя с уже зарегистрированной почтой")
    void dataIntegrityViolation_returns409() {
        ResponseEntity<Map<String, String>> response = advice.handleDataIntegrity(new DataIntegrityViolationException("dup"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("error", "Email already registered");
    }
}

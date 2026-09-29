package com.github.interviewbeaterservice.config;

import com.github.interviewbeaterservice.auth.exception.BadCredentialsException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ControllerAdviceTest {

    private final ControllerAdvice advice = new ControllerAdvice();

    @Test
    void badCredentials_returns401() {
        ResponseEntity<Map<String, String>> response = advice.handleBadCredentials(new BadCredentialsException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("error", "Invalid email or password");
    }

    @Test
    void dataIntegrityViolation_returns409() {
        ResponseEntity<Map<String, String>> response = advice.handleDataIntegrity(new DataIntegrityViolationException("dup"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("error", "Email already registered");
    }
}

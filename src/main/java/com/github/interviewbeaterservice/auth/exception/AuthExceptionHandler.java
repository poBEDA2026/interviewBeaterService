package com.github.interviewbeaterservice.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.security.sasl.AuthenticationException;
import java.util.Map;

@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Bad credentials"));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Bad credentials"));
    }
}

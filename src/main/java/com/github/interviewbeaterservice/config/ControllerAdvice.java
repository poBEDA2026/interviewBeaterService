package com.github.interviewbeaterservice.config;

import com.github.interviewbeaterservice.question.exception.AttachmentNotFoundException;
import com.github.interviewbeaterservice.question.exception.InvalidAttachmentException;
import com.github.interviewbeaterservice.question.exception.QuestionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(QuestionNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleQuestionNotFound(QuestionNotFoundException e) {
        return body(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(AttachmentNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleAttachmentNotFound(AttachmentNotFoundException e) {
        return body(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(InvalidAttachmentException.class)
    public ResponseEntity<Map<String, String>> handleInvalidAttachment(InvalidAttachmentException e) {
        return body(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "Validation failed");
        body.put("fields", e.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of("field", fe.getField(), "message", fe.getDefaultMessage() == null ? "" : fe.getDefaultMessage()))
                .toList());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleUploadTooLarge(MaxUploadSizeExceededException e) {
        return body(HttpStatus.PAYLOAD_TOO_LARGE, "Upload is too large");
    }

    private static ResponseEntity<Map<String, String>> body(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("error", message));
    }
}

package com.github.interviewbeaterservice.auth.exception;

public class BadCredentialsException extends RuntimeException {
    public BadCredentialsException() {
        super("Bad credentials");
    }
}

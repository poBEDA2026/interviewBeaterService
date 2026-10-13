package com.github.interviewbeaterservice.auth.exception;

public class BadCredentialsException extends RuntimeException {

  /**
   * Создаёт исключение со стандартным сообщением «Bad credentials».
   */
  public BadCredentialsException() {
    super("Bad credentials");
  }
}

package com.github.interviewbeaterservice.question.exception;

public class AttachmentNotFoundException extends RuntimeException {

  /**
   * Создаёт исключение с сообщением о ненайденном вложении в рамках указанного вопроса.
   *
   * @param questionId   ID вопроса
   * @param attachmentId ID вложения
   */
  public AttachmentNotFoundException(Long questionId, Long attachmentId) {
    super("Attachment " + attachmentId + " for question " + questionId + " not found");
  }
}

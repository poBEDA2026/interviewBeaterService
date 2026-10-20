package com.github.interviewbeaterservice.question.controller;

import com.github.interviewbeaterservice.question.dto.AttachmentResponse;
import com.github.interviewbeaterservice.question.service.QuestionAttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.net.URL;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/questions/{questionId}/attachments")
@RequiredArgsConstructor
@Tag(name = "Questions", description = "Вложения к вопросам")
public class QuestionAttachmentController {

  private final QuestionAttachmentService service;

  /**
   * Загрузить один или несколько файлов как вложения к вопросу.
   *
   * @param questionId ID вопроса
   * @param files      список загружаемых файлов
   * @return DTO созданных вложений
   */
  @Operation(summary = "Загрузить файлы к вопросу (1..N)")
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public List<AttachmentResponse> upload(
      @PathVariable Long questionId,
      @RequestParam("files") List<MultipartFile> files) {
    return service.upload(questionId, files);
  }

  /**
   * Получить список всех вложений вопроса.
   *
   * @param questionId ID вопроса
   * @return список DТО вложений
   */
  @Operation(summary = "Список вложений вопроса")
  @GetMapping
  public List<AttachmentResponse> list(@PathVariable Long questionId) {
    return service.list(questionId);
  }

  /**
   * Получить 302 redirect на pre-signed URL для скачивания вложения.
   *
   * @param questionId   ID вопроса
   * @param attachmentId ID вложения
   * @return ответ с заголовком Location, указывающим на pre-signed URL
   */
  @Operation(summary = "302 redirect на pre-signed S3 URL")
  @GetMapping("/{attachmentId}")
  public ResponseEntity<Void> download(
      @PathVariable Long questionId,
      @PathVariable Long attachmentId) {
    URL url = service.presignedDownloadUrl(questionId, attachmentId);
    return ResponseEntity.status(HttpStatus.FOUND)
        .location(URI.create(url.toString()))
        .build();
  }

  /**
   * Удалить вложение у вопроса.
   *
   * @param questionId   ID вопроса
   * @param attachmentId ID вложения
   */
  @Operation(summary = "Удалить вложение")
  @DeleteMapping("/{attachmentId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @PathVariable Long questionId,
      @PathVariable Long attachmentId) {
    service.delete(questionId, attachmentId);
  }
}

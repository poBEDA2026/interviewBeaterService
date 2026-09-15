package com.github.interviewbeaterservice.question.controller;

import com.github.interviewbeaterservice.question.dto.AttachmentResponse;
import com.github.interviewbeaterservice.question.service.QuestionAttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.URL;
import java.util.List;

@RestController
@RequestMapping("/questions/{questionId}/attachments")
@RequiredArgsConstructor
@Tag(name = "Questions", description = "Вложения к вопросам")
public class QuestionAttachmentController {

    private final QuestionAttachmentService service;

    @Operation(summary = "Загрузить файлы к вопросу (1..N)")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<AttachmentResponse> upload(
            @PathVariable Long questionId,
            @RequestParam("files") List<MultipartFile> files) {
        return service.upload(questionId, files);
    }

    @Operation(summary = "Список вложений вопроса")
    @GetMapping
    public List<AttachmentResponse> list(@PathVariable Long questionId) {
        return service.list(questionId);
    }

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

    @Operation(summary = "Удалить вложение")
    @DeleteMapping("/{attachmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long questionId,
            @PathVariable Long attachmentId) {
        service.delete(questionId, attachmentId);
    }
}

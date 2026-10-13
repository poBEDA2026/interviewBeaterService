package com.github.interviewbeaterservice.question.service;

import com.github.interviewbeaterservice.question.dto.AttachmentResponse;
import com.github.interviewbeaterservice.question.entity.Attachment;
import com.github.interviewbeaterservice.question.entity.Question;
import com.github.interviewbeaterservice.question.exception.AttachmentNotFoundException;
import com.github.interviewbeaterservice.question.exception.InvalidAttachmentException;
import com.github.interviewbeaterservice.question.exception.QuestionNotFoundException;
import com.github.interviewbeaterservice.question.repository.AttachmentRepository;
import com.github.interviewbeaterservice.question.repository.QuestionRepository;
import com.github.interviewbeaterservice.storage.StorageProperties;
import com.github.interviewbeaterservice.storage.StorageService;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionAttachmentService {

  private static final String KEY_PREFIX = "questions/";

  private final QuestionRepository questionRepository;
  private final AttachmentRepository attachmentRepository;
  private final StorageService storageService;
  private final StorageProperties properties;

  /**
   * Загрузить вложения к вопросу: проверить файлы, положить их в хранилище и сохранить метаданные в
   * БД.
   *
   * @param questionId ID вопроса
   * @param files      список загружаемых файлов
   * @return DTO созданных вложений
   * @throws InvalidAttachmentException если файлы не прошли валидацию (размер, тип, количество)
   * @throws QuestionNotFoundException  если вопрос с указанным ID не найден
   */
  @Transactional
  public List<AttachmentResponse> upload(Long questionId, List<MultipartFile> files) {
    validateFiles(files);

    Question question = questionRepository.findById(questionId)
        .orElseThrow(() -> new QuestionNotFoundException(questionId));

    Set<String> allowed = properties.allowedContentTypes();

    return files.stream().map(file -> {
      String contentType = normalizeContentType(file.getContentType());
      if (!allowed.contains(contentType)) {
        throw new InvalidAttachmentException(
            "Content-Type '" + contentType + "' is not allowed");
      }

      String storageKey = buildKey(questionId, file.getOriginalFilename());
      try {
        storageService.upload(
            storageKey,
            file.getInputStream(),
            file.getSize(),
            contentType);
      } catch (IOException e) {
        throw new InvalidAttachmentException(
            "Failed to read uploaded file: " + e.getMessage());
      }

      Attachment attachment = Attachment.builder()
          .storageKey(storageKey)
          .originalName(file.getOriginalFilename())
          .contentType(contentType)
          .sizeBytes(file.getSize())
          .build();
      attachment.setQuestion(question);

      Attachment saved = attachmentRepository.save(attachment);
      return AttachmentResponse.from(saved);
    }).toList();
  }

  /**
   * Получить список всех вложений указанного вопроса.
   *
   * @param questionId ID вопроса
   * @return DTO вложений вопроса
   * @throws QuestionNotFoundException если вопрос с указанным ID не найден
   */
  public List<AttachmentResponse> list(Long questionId) {
    if (!questionRepository.existsById(questionId)) {
      throw new QuestionNotFoundException(questionId);
    }
    return attachmentRepository.findAllByQuestionId(questionId).stream()
        .map(AttachmentResponse::from)
        .toList();
  }

  /**
   * Сгенерировать pre-signed URL для скачивания вложения.
   *
   * @param questionId   ID вопроса
   * @param attachmentId ID вложения
   * @return pre-signed URL для скачивания
   * @throws AttachmentNotFoundException если вложение не найдено в рамках вопроса
   */
  public URL presignedDownloadUrl(Long questionId, Long attachmentId) {
    Attachment attachment = attachmentRepository
        .findByQuestionIdAndId(questionId, attachmentId)
        .orElseThrow(() -> new AttachmentNotFoundException(questionId, attachmentId));

    Duration ttl = properties.presignedUrlTtl();
    return storageService.presignedGetUrl(attachment.getStorageKey(), ttl);
  }

  /**
   * Удалить вложение: сначала удаляет объект из хранилища, затем запись из БД.
   *
   * @param questionId   ID вопроса
   * @param attachmentId ID вложения
   * @throws AttachmentNotFoundException если вложение не найдено в рамках вопроса
   */
  @Transactional
  public void delete(Long questionId, Long attachmentId) {
    Attachment attachment = attachmentRepository
        .findByQuestionIdAndId(questionId, attachmentId)
        .orElseThrow(() -> new AttachmentNotFoundException(questionId, attachmentId));

    // Сначала storage, потом БД. Если DB-delete упадёт — остаётся orphan-объект
    // в S3, что безопаснее, чем DB-строка, ссылающаяся на несуществующий объект.
    storageService.delete(attachment.getStorageKey());
    attachmentRepository.delete(attachment);
  }

  /**
   * Проверить загружаемые файлы: наличие, размер каждого файла, общее количество файлов.
   *
   * @param files список загружаемых файлов
   * @throws InvalidAttachmentException если хотя бы одна проверка не прошла
   */
  private void validateFiles(List<MultipartFile> files) {
    if (files == null || files.isEmpty()) {
      throw new InvalidAttachmentException("At least one file is required");
    }
    if (files.size() > properties.maxFilesPerUpload()) {
      throw new InvalidAttachmentException(
          "Too many files: " + files.size() + " (max " + properties.maxFilesPerUpload() + ")");
    }
    for (MultipartFile file : files) {
      if (file == null || file.isEmpty()) {
        throw new InvalidAttachmentException("File is empty");
      }
      if (file.getSize() > properties.maxFileSizeBytes()) {
        throw new InvalidAttachmentException(
            "File '" + file.getOriginalFilename() + "' is too large: "
                + file.getSize() + " bytes (max " + properties.maxFileSizeBytes() + ")");
      }
    }
  }

  /**
   * Привести Content-Type к нижнему регистру, чтобы избежать чувствительности к регистру при
   * сравнении с allow-list.
   *
   * @param contentType исходный Content-Type
   * @return нормализованный Content-Type (пустая строка для {@code null})
   */
  private static String normalizeContentType(String contentType) {
    return contentType == null ? "" : contentType.toLowerCase();
  }

  /**
   * Сгенерировать storage key для загружаемого файла с защитой от path traversal: расширение
   * берётся только если оно алфавитно-цифровое и длиной 1..8 символов.
   *
   * @param questionId   ID вопроса
   * @param originalName исходное имя файла
   * @return storage key вида {@code questions/<questionId>/<uuid>.<ext>}
   */
  private static String buildKey(Long questionId, String originalName) {
    String ext = "";
    if (originalName != null) {
      int dot = originalName.lastIndexOf('.');
      // защита от path traversal: берём только алфавитно-цифровое расширение
      if (dot >= 0 && dot < originalName.length() - 1) {
        String candidate = originalName.substring(dot + 1).toLowerCase();
        if (candidate.matches("[a-z0-9]{1,8}")) {
          ext = "." + candidate;
        }
      }
    }
    return KEY_PREFIX + questionId + "/" + UUID.randomUUID() + ext;
  }
}

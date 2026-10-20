package com.github.interviewbeaterservice.storage;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Slf4j
@Service
@RequiredArgsConstructor
class S3StorageService implements StorageService {

  private final StorageProperties properties;
  private final S3Client s3Client;
  private final S3Presigner s3Presigner;

  /**
   * Положить объект в хранилище.
   *
   * @param key         storage key (путь внутри бакета)
   * @param content     поток с байтами объекта
   * @param size        размер объекта в байтах
   * @param contentType MIME-тип объекта
   */
  @Override
  public void upload(String key, InputStream content, long size, String contentType) {
    PutObjectRequest request = PutObjectRequest.builder()
        .bucket(properties.bucket())
        .key(key)
        .contentType(contentType)
        .contentLength(size)
        .build();

    s3Client.putObject(request, RequestBody.fromInputStream(content, size));
    log.debug("Uploaded object '{}' to bucket '{}' ({} bytes)", key, properties.bucket(), size);
  }

  /**
   * Удалить объект по ключу. Идемпотентно: ошибки «не найдено» не бросаем.
   *
   * @param key storage key удаляемого объекта
   */
  @Override
  public void delete(String key) {
    DeleteObjectRequest request = DeleteObjectRequest.builder()
        .bucket(properties.bucket())
        .key(key)
        .build();

    s3Client.deleteObject(request);
    log.debug("Deleted object '{}' from bucket '{}'", key, properties.bucket());
  }

  /**
   * Сгенерировать временный URL для скачивания объекта.
   *
   * @param key storage key объекта
   * @param ttl время жизни подписи
   * @return pre-signed URL для скачивания объекта
   */
  @Override
  public URL presignedGetUrl(String key, Duration ttl) {
    return s3Presigner.presignGetObject(builder -> builder
            .getObjectRequest(getObject -> getObject
                .bucket(properties.bucket())
                .key(key))
            .signatureDuration(ttl))
        .url();
  }
}

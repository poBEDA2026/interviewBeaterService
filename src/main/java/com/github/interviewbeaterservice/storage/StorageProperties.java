package com.github.interviewbeaterservice.storage;

import jakarta.validation.constraints.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.Set;

/**
 * Настройки S3-совместимого хранилища для вложений.
 * Префикс в application.yml: {@code storage}.
 *
 * @param endpoint            endpoint хранилища (например, http://localhost:9000)
 * @param region              регион хранилища
 * @param bucket              имя бакета для вложений
 * @param accessKey           access-ключ
 * @param secretKey           secret-ключ
 * @param maxFileSizeBytes    максимальный размер одного файла в байтах
 * @param maxFilesPerUpload   максимум файлов в одной загрузке
 * @param allowedContentTypes белый список допустимых Content-Type
 * @param presignedUrlTtl     время жизни pre-signed URL
 */
@Validated
@ConfigurationProperties(prefix = "storage")
public record StorageProperties(
        @NotBlank String endpoint,
        @NotBlank String region,
        @NotBlank String bucket,
        @NotBlank String accessKey,
        @NotBlank String secretKey,
        @Positive long maxFileSizeBytes,
        @Min(1) @Max(50) int maxFilesPerUpload,
        @NotEmpty Set<String> allowedContentTypes,
        Duration presignedUrlTtl
) {
}

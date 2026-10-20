package com.github.interviewbeaterservice.storage;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Настройки S3-совместимого хранилища для вложений. Префикс в application.yml: {@code storage}.
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

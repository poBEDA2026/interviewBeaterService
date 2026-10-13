package com.github.interviewbeaterservice.storage;

import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(StorageProperties.class)
public class S3Config {

  private final StorageProperties properties;

  /**
   * Создаёт клиент S3, сконфигурированный под текущее хранилище (endpoint, регион, статические
   * креды).
   *
   * @return сконфигурированный экземпляр {@link S3Client}
   */
  @Bean
  public S3Client s3Client() {
    return S3Client.builder()
        .endpointOverride(URI.create(properties.endpoint()))
        .forcePathStyle(true)
        .credentialsProvider(StaticCredentialsProvider.create(
            AwsBasicCredentials.create(properties.accessKey(), properties.secretKey())))
        .region(Region.of(properties.region()))
        .build();
  }

  /**
   * Создаёт пресайнер для генерации временных URL на скачивание объектов.
   *
   * @return сконфигурированный экземпляр {@link S3Presigner}
   */
  @Bean
  public S3Presigner s3Presigner() {
    return S3Presigner.builder()
        .endpointOverride(URI.create(properties.endpoint()))
        .credentialsProvider(StaticCredentialsProvider.create(
            AwsBasicCredentials.create(properties.accessKey(), properties.secretKey())))
        .region(Region.of(properties.region()))
        .build();
  }

  /**
   * Идемпотентное создание бакета при старте приложения. Если бакет уже есть — логирует и
   * продолжает работу.
   *
   * @param s3Client клиент S3 для проверки и создания бакета
   * @return раннер, выполняющий инициализацию бакета при старте
   */
  @Bean
  public ApplicationRunner s3BucketInitializer(S3Client s3Client) {
    return args -> {
      String bucket = properties.bucket();
      try {
        s3Client.headBucket(b -> b.bucket(bucket));
        log.info("S3 bucket '{}' already exists", bucket);
      } catch (NoSuchBucketException e) {
        try {
          s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
          log.info("Created S3 bucket '{}'", bucket);
        } catch (BucketAlreadyOwnedByYouException race) {
          // Другой инстанс успел создать — это нормально.
          log.info("S3 bucket '{}' was created concurrently", bucket);
        }
      }
    };
  }
}

package com.github.interviewbeaterservice.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
class S3StorageService implements StorageService {

    private final StorageProperties properties;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

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

    @Override
    public void delete(String key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(properties.bucket())
                .key(key)
                .build();

        s3Client.deleteObject(request);
        log.debug("Deleted object '{}' from bucket '{}'", key, properties.bucket());
    }

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

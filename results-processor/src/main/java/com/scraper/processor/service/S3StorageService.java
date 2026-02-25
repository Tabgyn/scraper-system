package com.scraper.processor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageService {

    private final S3Client s3Client;

    @Value("${scraper.s3.bucket}")
    private String bucket;

    public void store(String key, String content) {
        try {
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(detectContentType(key))
                    .contentLength((long) bytes.length)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(bytes));
            log.info("Stored raw content to S3: bucket={}, key={}, size={}b",
                    bucket, key, bytes.length);

        } catch (Exception e) {
            log.error("Failed to store to S3: key={}", key, e);
            throw new RuntimeException("S3 storage failed for key: " + key, e);
        }
    }

    private String detectContentType(String key) {
        if (key.endsWith(".json")) return "application/json";
        if (key.endsWith(".html")) return "text/html";
        return "text/plain";
    }
}
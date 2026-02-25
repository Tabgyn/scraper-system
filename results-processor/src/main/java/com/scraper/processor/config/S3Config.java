package com.scraper.processor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class S3Config {

    @Value("${scraper.s3.endpoint:#{null}}")
    private String endpoint;

    @Value("${scraper.s3.region}")
    private String region;

    @Value("${scraper.s3.access-key}")
    private String accessKey;

    @Value("${scraper.s3.secret-key}")
    private String secretKey;

    @Bean
    public S3Client s3Client() {
        var builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)));

        // Allow custom endpoint for local MinIO
        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint))
                    .forcePathStyle(true);
        }

        return builder.build();
    }
}
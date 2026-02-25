package com.scraper.api.dto;

import com.scraper.common.enums.JobStatus;
import com.scraper.common.enums.JobType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class JobResponse {
    private UUID id;
    private String url;
    private JobType jobType;
    private JobStatus status;
    private int attemptNumber;
    private int maxAttempts;
    private Map<String, String> headers;
    private String extractorConfigId;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant scheduledAt;
}
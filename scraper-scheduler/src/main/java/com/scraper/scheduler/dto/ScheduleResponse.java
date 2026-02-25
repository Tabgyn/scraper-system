package com.scraper.scheduler.dto;

import com.scraper.common.enums.JobType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class ScheduleResponse {
    private UUID id;
    private String url;
    private JobType jobType;
    private String cronExpression;
    private boolean active;
    private int maxAttempts;
    private String extractorConfigId;
    private Map<String, String> headers;
    private Instant createdAt;
    private Instant updatedAt;
}
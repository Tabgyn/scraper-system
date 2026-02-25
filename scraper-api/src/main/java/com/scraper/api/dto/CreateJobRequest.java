package com.scraper.api.dto;

import com.scraper.common.enums.JobType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
public class CreateJobRequest {

    @NotBlank
    private String url;

    @NotNull
    private JobType jobType;

    private Map<String, String> headers;
    private String extractorConfigId;
    private Instant scheduledAt;

    private int maxAttempts = 3;
}
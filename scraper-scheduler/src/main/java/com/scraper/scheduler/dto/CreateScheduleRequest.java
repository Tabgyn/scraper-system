package com.scraper.scheduler.dto;

import com.scraper.common.enums.JobType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class CreateScheduleRequest {

    @NotBlank
    private String url;

    @NotNull
    private JobType jobType;

    @NotBlank
    private String cronExpression;

    private Map<String, String> headers;
    private String extractorConfigId;
    private int maxAttempts = 3;
}
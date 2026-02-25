package com.scraper.common.model;

import com.scraper.common.enums.JobStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScrapeResult {

    private UUID id;
    private UUID jobId;
    private String url;
    private JobStatus status;
    private int httpStatusCode;
    private String s3RawKey;
    private Map<String, Object> parsedData;   // flexible extracted fields
    private String errorMessage;
    private long durationMs;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant completedAt;
}
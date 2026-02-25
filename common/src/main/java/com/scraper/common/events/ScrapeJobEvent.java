package com.scraper.common.events;

import com.scraper.common.enums.JobType;
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
public class ScrapeJobEvent {

    private UUID jobId;
    private String url;
    private JobType jobType;
    private int attemptNumber;
    private int maxAttempts;
    private Map<String, String> headers;
    private String extractorConfigId;  // references extractor config in MongoDB

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant scheduledAt;
}
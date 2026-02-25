package com.scraper.common.events;

import com.scraper.common.enums.JobStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScrapeResultEvent {

    private UUID jobId;
    private String url;
    private JobStatus status;
    private int httpStatusCode;
    private String s3RawKey;        // pointer to raw dump in S3
    private String errorMessage;    // null on success
    private long durationMs;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant completedAt;
}
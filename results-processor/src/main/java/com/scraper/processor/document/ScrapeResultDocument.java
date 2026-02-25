package com.scraper.processor.document;

import com.scraper.common.enums.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "scrape_results")
public class ScrapeResultDocument {

    @Id
    private String id;

    @Indexed
    private UUID jobId;

    @Indexed
    private String url;

    private JobStatus status;
    private int httpStatusCode;
    private String s3RawKey;
    private Map<String, Object> parsedData;
    private String errorMessage;
    private long durationMs;

    @Indexed
    private Instant completedAt;
}
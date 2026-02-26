package com.scraper.worker.service;

import com.scraper.common.enums.JobStatus;
import com.scraper.common.events.ScrapeResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResultPublisher {

    private static final String TOPIC_RESULTS_RAW = "results.raw";
    private static final String TOPIC_JOBS_RETRY  = "jobs.retry";
    private static final String TOPIC_JOBS_DLQ    = "jobs.dlq";

    private final KafkaTemplate<String, ScrapeResultEvent> kafkaTemplate;

    public void publishSuccess(UUID jobId, String url, int httpStatus,
                               String s3RawKey, String rawContent, long durationMs) {
        ScrapeResultEvent event = ScrapeResultEvent.builder()
                .jobId(jobId)
                .url(url)
                .status(JobStatus.COMPLETED)
                .httpStatusCode(httpStatus)
                .s3RawKey(s3RawKey)
                .rawContent(rawContent)   // ← include content
                .durationMs(durationMs)
                .completedAt(Instant.now())
                .build();

        kafkaTemplate.send(TOPIC_RESULTS_RAW, jobId.toString(), event);
        log.info("Result published: jobId={}, status=COMPLETED, url={}", jobId, url);
    }

    public void publishFailure(UUID jobId, String url, String errorMessage,
                               int attemptNumber, int maxAttempts, long durationMs) {
        boolean exhausted = attemptNumber >= maxAttempts;
        String topic = exhausted ? TOPIC_JOBS_DLQ : TOPIC_JOBS_RETRY;
        JobStatus status = exhausted ? JobStatus.DEAD : JobStatus.RETRYING;

        ScrapeResultEvent event = ScrapeResultEvent.builder()
                .jobId(jobId)
                .url(url)
                .status(status)
                .errorMessage(errorMessage)
                .durationMs(durationMs)
                .completedAt(Instant.now())
                .build();

        kafkaTemplate.send(topic, jobId.toString(), event);
        log.warn("Result published: jobId={}, status={}, attempt={}/{}, url={}",
                jobId, status, attemptNumber, maxAttempts, url);
    }
}
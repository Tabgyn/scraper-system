package com.scraper.processor.service;

import com.scraper.common.events.ScrapeResultEvent;
import com.scraper.processor.document.ScrapeResultDocument;
import com.scraper.processor.repository.ScrapeResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResultPersistenceService {

    private final ScrapeResultRepository repository;

    public void persist(ScrapeResultEvent event) {
        ScrapeResultDocument document = ScrapeResultDocument.builder()
                .jobId(event.getJobId())
                .url(event.getUrl())
                .status(event.getStatus())
                .httpStatusCode(event.getHttpStatusCode())
                .s3RawKey(event.getS3RawKey())
                .errorMessage(event.getErrorMessage())
                .durationMs(event.getDurationMs())
                .completedAt(event.getCompletedAt())
                .build();

        repository.save(document);
        log.info("Result persisted to MongoDB: jobId={}, status={}",
                event.getJobId(), event.getStatus());
    }
}
package com.scraper.processor.consumer;

import com.scraper.common.enums.JobStatus;
import com.scraper.common.events.ScrapeResultEvent;
import com.scraper.processor.metrics.ScraperProcessorMetrics;
import com.scraper.processor.service.DeduplicationService;
import com.scraper.processor.service.ResultPersistenceService;
import com.scraper.processor.service.S3StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResultEventConsumer {

    private final DeduplicationService deduplicationService;
    private final S3StorageService s3StorageService;
    private final ResultPersistenceService resultPersistenceService;
    private final ScraperProcessorMetrics scraperProcessorMetrics;

    @KafkaListener(
            topics = "results.raw",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ScrapeResultEvent event, Acknowledgment ack) {
        log.info("Received result event: jobId={}, status={}", event.getJobId(), event.getStatus());

        try {
            if (!deduplicationService.isNew(event.getJobId())) {
                scraperProcessorMetrics.recordDuplicate();
                ack.acknowledge();
                return;
            }

            if (event.getStatus() == JobStatus.COMPLETED && event.getS3RawKey() != null) {
                s3StorageService.store(event.getS3RawKey(), buildMetadataPlaceholder(event));
                scraperProcessorMetrics.recordS3Stored();
            }

            resultPersistenceService.persist(event);
            scraperProcessorMetrics.recordMongoStored();
            scraperProcessorMetrics.recordProcessed();

            ack.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process result event: jobId={}", event.getJobId(), e);
            // Do not ack — Kafka will redeliver
        }
    }

    private String buildMetadataPlaceholder(ScrapeResultEvent event) {
        if (event.getStatus() == JobStatus.COMPLETED && event.getS3RawKey() != null) {
            String content = event.getRawContent() != null
                    ? event.getRawContent()
                    : buildMetadataPlaceholder(event);

            s3StorageService.store(event.getS3RawKey(), content);
            scraperProcessorMetrics.recordS3Stored();
        }

        return "{\"jobId\":\"" + event.getJobId() + "\""
                + ",\"url\":\"" + event.getUrl() + "\""
                + ",\"completedAt\":\"" + event.getCompletedAt() + "\"}";
    }
}
package com.scraper.processor.consumer;

import com.scraper.common.enums.JobStatus;
import com.scraper.common.events.ScrapeResultEvent;
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

    @KafkaListener(
            topics = "results.raw",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ScrapeResultEvent event, Acknowledgment ack) {
        log.info("Received result event: jobId={}, status={}", event.getJobId(), event.getStatus());

        try {
            if (!deduplicationService.isNew(event.getJobId())) {
                ack.acknowledge();
                return;
            }

            // Only store raw content for successful scrapes
            if (event.getStatus() == JobStatus.COMPLETED && event.getS3RawKey() != null) {
                // Raw content would be passed through the event in a full implementation
                // Here we store a metadata placeholder — real content comes from the worker
                s3StorageService.store(event.getS3RawKey(),
                        buildMetadataPlaceholder(event));
            }

            resultPersistenceService.persist(event);
            ack.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process result event: jobId={}", event.getJobId(), e);
            // Do not ack — Kafka will redeliver
        }
    }

    private String buildMetadataPlaceholder(ScrapeResultEvent event) {
        return String.format(
                "{\"jobId\":\"%s\",\"url\":\"%s\",\"completedAt\":\"%s\"}",
                event.getJobId(), event.getUrl(), event.getCompletedAt()
        );
    }
}
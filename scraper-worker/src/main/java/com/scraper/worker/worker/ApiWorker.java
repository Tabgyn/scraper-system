package com.scraper.worker.worker;

import com.scraper.common.enums.JobType;
import com.scraper.common.events.ScrapeJobEvent;
import com.scraper.worker.metrics.WorkerMetrics;
import com.scraper.worker.proxy.ProxyManager;
import com.scraper.worker.service.ResultPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiWorker {

    private final WebClient.Builder webClientBuilder;
    private final ProxyManager proxyManager;
    private final ResultPublisher resultPublisher;
    private final WorkerMetrics workerMetrics;

    @KafkaListener(
            topics = "jobs.pending",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ScrapeJobEvent event, Acknowledgment ack) {
        if (event.getJobType() != JobType.API) {
            ack.acknowledge();
            return;
        }

        long start = Instant.now().toEpochMilli();
        String domain = extractDomain(event.getUrl());

        log.info("API worker processing: jobId={}, url={}", event.getJobId(), event.getUrl());

        try {
            if (!proxyManager.isAllowed(domain)) {
                Thread.sleep(1000);
            }

            var spec = webClientBuilder.build().get().uri(event.getUrl());

            if (event.getHeaders() != null) {
                event.getHeaders().forEach(spec::header);
            }

            String responseBody = spec.retrieve()
                    .bodyToMono(String.class)
                    .block();

            long duration = Instant.now().toEpochMilli() - start;
            String s3Key = "raw/" + event.getJobId() + ".json";

            resultPublisher.publishSuccess(
                    event.getJobId(),
                    event.getUrl(),
                    200,
                    s3Key,
                    responseBody,  // ← pass actual content
                    duration);
            workerMetrics.recordSuccess(JobType.HTTP, duration);
            ack.acknowledge();

        } catch (Exception ex) {
            long duration = Instant.now().toEpochMilli() - start;
            resultPublisher.publishFailure(event.getJobId(), event.getUrl(),
                    ex.getMessage(), event.getAttemptNumber(), event.getMaxAttempts(), duration);
            workerMetrics.recordFailure(JobType.HTTP, duration);
            ack.acknowledge();
        }
    }

    private String extractDomain(String url) {
        try {
            return URI.create(url).getHost();
        } catch (Exception e) {
            return url;
        }
    }
}
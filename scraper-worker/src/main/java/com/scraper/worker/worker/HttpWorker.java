package com.scraper.worker.worker;

import com.scraper.common.events.ScrapeJobEvent;
import com.scraper.common.model.ProxyConfig;
import com.scraper.worker.proxy.ProxyManager;
import com.scraper.worker.service.ResultPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.URI;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class HttpWorker {

    private final WebClient.Builder webClientBuilder;
    private final ProxyManager proxyManager;
    private final ResultPublisher resultPublisher;

    @KafkaListener(
            topics = "jobs.pending",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ScrapeJobEvent event, Acknowledgment ack) {
        if (event.getJobType() != com.scraper.common.enums.JobType.HTTP) {
            ack.acknowledge();
            return;
        }

        long start = Instant.now().toEpochMilli();
        String domain = extractDomain(event.getUrl());

        log.info("HTTP worker processing: jobId={}, url={}", event.getJobId(), event.getUrl());

        try {
            if (!proxyManager.isAllowed(domain)) {
                Thread.sleep(1000); // back off 1s on rate limit
            }

            ProxyConfig proxy = proxyManager.nextProxy();
            String rawHtml = fetchWithWebClient(event, proxy);

            long duration = Instant.now().toEpochMilli() - start;
            // S3 key placeholder — wired in results-processor
            String s3Key = "raw/" + event.getJobId() + ".html";

            resultPublisher.publishSuccess(event.getJobId(), event.getUrl(),
                    200, s3Key, duration);
            ack.acknowledge();

        } catch (WebClientResponseException ex) {
            long duration = Instant.now().toEpochMilli() - start;
            log.error("HTTP error: jobId={}, status={}", event.getJobId(), ex.getStatusCode());
            resultPublisher.publishFailure(event.getJobId(), event.getUrl(),
                    ex.getMessage(), event.getAttemptNumber(), event.getMaxAttempts(), duration);
            ack.acknowledge();

        } catch (Exception ex) {
            long duration = Instant.now().toEpochMilli() - start;
            log.error("Unexpected error: jobId={}", event.getJobId(), ex);
            resultPublisher.publishFailure(event.getJobId(), event.getUrl(),
                    ex.getMessage(), event.getAttemptNumber(), event.getMaxAttempts(), duration);
            ack.acknowledge();
        }
    }

    private String fetchWithWebClient(ScrapeJobEvent event, ProxyConfig proxy) {
        WebClient client = webClientBuilder.build();

        var spec = client.get().uri(event.getUrl());

        if (event.getHeaders() != null) {
            event.getHeaders().forEach(spec::header);
        }

        return spec.retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private String extractDomain(String url) {
        try {
            return URI.create(url).getHost();
        } catch (Exception e) {
            return url;
        }
    }
}
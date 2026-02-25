package com.scraper.worker.worker;

import com.microsoft.playwright.*;
import com.scraper.common.enums.JobType;
import com.scraper.common.events.ScrapeJobEvent;
import com.scraper.common.model.ProxyConfig;
import com.scraper.worker.proxy.ProxyManager;
import com.scraper.worker.service.ResultPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsWorker {

    private final Browser browser;
    private final ProxyManager proxyManager;
    private final ResultPublisher resultPublisher;

    @KafkaListener(
            topics = "jobs.pending",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ScrapeJobEvent event, Acknowledgment ack) {
        if (event.getJobType() != JobType.JS) {
            ack.acknowledge();
            return;
        }

        long start = Instant.now().toEpochMilli();
        log.info("JS worker processing: jobId={}, url={}", event.getJobId(), event.getUrl());

        BrowserContext context = null;
        try {
            ProxyConfig proxy = proxyManager.nextProxy();
            Browser.NewContextOptions contextOptions = buildContextOptions(proxy, event);
            context = browser.newContext(contextOptions);

            Page page = context.newPage();
            page.setDefaultTimeout(30_000);

            Response response = page.navigate(event.getUrl());
            page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);

            String html = page.content();
            int status = response != null ? response.status() : 200;
            long duration = Instant.now().toEpochMilli() - start;

            String s3Key = "raw/" + event.getJobId() + ".html";
            resultPublisher.publishSuccess(event.getJobId(), event.getUrl(),
                    status, s3Key, duration);
            ack.acknowledge();

        } catch (Exception ex) {
            long duration = Instant.now().toEpochMilli() - start;
            log.error("JS worker error: jobId={}", event.getJobId(), ex);
            resultPublisher.publishFailure(event.getJobId(), event.getUrl(),
                    ex.getMessage(), event.getAttemptNumber(), event.getMaxAttempts(), duration);
            ack.acknowledge();

        } finally {
            if (context != null) context.close();
        }
    }

    private Browser.NewContextOptions buildContextOptions(ProxyConfig proxy, ScrapeJobEvent event) {
        Browser.NewContextOptions options = new Browser.NewContextOptions()
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                        + "AppleWebKit/537.36 (KHTML, like Gecko) "
                        + "Chrome/128.0.0.0 Safari/537.36")
                .setViewportSize(1920, 1080)
                .setJavaScriptEnabled(true);

        if (proxy != null) {
            com.microsoft.playwright.options.Proxy playwrightProxy =
                    new com.microsoft.playwright.options.Proxy(
                            proxy.getHost() + ":" + proxy.getPort());
            if (proxy.getUsername() != null) {
                playwrightProxy.setUsername(proxy.getUsername());
                playwrightProxy.setPassword(proxy.getPassword());
            }
            options.setProxy(playwrightProxy);
        }

        if (event.getHeaders() != null) {
            options.setExtraHTTPHeaders(event.getHeaders());
        }

        return options;
    }
}
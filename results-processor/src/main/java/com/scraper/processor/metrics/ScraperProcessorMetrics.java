package com.scraper.processor.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ScraperProcessorMetrics {

    private final MeterRegistry registry;

    public ScraperProcessorMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordProcessed() {
        Counter.builder("scraper.results.processed")
                .tag("status", "success")
                .register(registry)
                .increment();
    }

    public void recordDuplicate() {
        Counter.builder("scraper.results.duplicates")
                .register(registry)
                .increment();
    }

    public void recordS3Stored() {
        Counter.builder("scraper.s3.stored")
                .register(registry)
                .increment();
    }

    public void recordMongoStored() {
        Counter.builder("scraper.mongo.stored")
                .register(registry)
                .increment();
    }
}
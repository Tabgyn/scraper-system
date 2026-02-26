package com.scraper.worker.metrics;

import com.scraper.common.enums.JobType;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class WorkerMetrics {

    private final MeterRegistry registry;

    public WorkerMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordSuccess(JobType jobType, long durationMs) {
        Counter.builder("scraper.jobs.completed")
                .tag("job_type", jobType.name())
                .tag("status", "success")
                .register(registry)
                .increment();

        Timer.builder("scraper.jobs.duration")
                .tag("job_type", jobType.name())
                .tag("status", "success")
                .register(registry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordFailure(JobType jobType, long durationMs) {
        Counter.builder("scraper.jobs.completed")
                .tag("job_type", jobType.name())
                .tag("status", "failure")
                .register(registry)
                .increment();

        Timer.builder("scraper.jobs.duration")
                .tag("job_type", jobType.name())
                .tag("status", "failure")
                .register(registry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordProxyPoolSize(int size) {
        registry.gauge("scraper.proxy.pool.size", size);
    }

    public void recordRateLimitHit(String domain) {
        Counter.builder("scraper.ratelimit.hits")
                .tag("domain", domain)
                .register(registry)
                .increment();
    }
}
package com.scraper.scheduler.job;

import com.scraper.common.enums.JobType;
import com.scraper.common.events.ScrapeJobEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScrapeQuartzJob implements Job {

    private static final String TOPIC_JOBS_PENDING = "jobs.pending";

    private final KafkaTemplate<String, ScrapeJobEvent> kafkaTemplate;

    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap data = context.getMergedJobDataMap();

        UUID jobId = UUID.randomUUID();
        String url = data.getString("url");
        JobType jobType = JobType.valueOf(data.getString("jobType"));
        int maxAttempts = data.getIntValue("maxAttempts");
        String extractorConfigId = data.getString("extractorConfigId");

        ScrapeJobEvent event = ScrapeJobEvent.builder()
                .jobId(jobId)
                .url(url)
                .jobType(jobType)
                .attemptNumber(0)
                .maxAttempts(maxAttempts)
                .extractorConfigId(extractorConfigId)
                .createdAt(Instant.now())
                .scheduledAt(Instant.now())
                .build();

        kafkaTemplate.send(TOPIC_JOBS_PENDING, jobId.toString(), event);
        log.info("Scheduled job emitted to Kafka: jobId={}, url={}", jobId, url);
    }
}
package com.scraper.api.mapper;

import com.scraper.api.dto.CreateJobRequest;
import com.scraper.api.dto.JobResponse;
import com.scraper.api.entity.JobEntity;
import com.scraper.common.enums.JobStatus;
import com.scraper.common.events.ScrapeJobEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class JobMapper {

    public JobEntity toEntity(CreateJobRequest request) {
        return JobEntity.builder()
                .url(request.getUrl())
                .jobType(request.getJobType())
                .status(JobStatus.PENDING)
                .attemptNumber(0)
                .maxAttempts(request.getMaxAttempts())
                .headers(request.getHeaders())
                .extractorConfigId(request.getExtractorConfigId())
                .scheduledAt(request.getScheduledAt() != null
                        ? request.getScheduledAt()
                        : Instant.now())
                .build();
    }

    public JobResponse toResponse(JobEntity entity) {
        return JobResponse.builder()
                .id(entity.getId())
                .url(entity.getUrl())
                .jobType(entity.getJobType())
                .status(entity.getStatus())
                .attemptNumber(entity.getAttemptNumber())
                .maxAttempts(entity.getMaxAttempts())
                .headers(entity.getHeaders())
                .extractorConfigId(entity.getExtractorConfigId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .scheduledAt(entity.getScheduledAt())
                .build();
    }

    public ScrapeJobEvent toEvent(JobEntity entity) {
        return ScrapeJobEvent.builder()
                .jobId(entity.getId())
                .url(entity.getUrl())
                .jobType(entity.getJobType())
                .attemptNumber(entity.getAttemptNumber())
                .maxAttempts(entity.getMaxAttempts())
                .headers(entity.getHeaders())
                .extractorConfigId(entity.getExtractorConfigId())
                .scheduledAt(entity.getScheduledAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
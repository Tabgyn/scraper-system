package com.scraper.api.service;

import com.scraper.api.dto.CreateJobRequest;
import com.scraper.api.dto.JobResponse;
import com.scraper.api.entity.JobEntity;
import com.scraper.api.mapper.JobMapper;
import com.scraper.api.repository.JobRepository;
import com.scraper.common.enums.JobStatus;
import com.scraper.common.events.ScrapeJobEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private static final String TOPIC_JOBS_PENDING = "jobs.pending";

    private final JobRepository jobRepository;
    private final JobMapper jobMapper;
    private final KafkaTemplate<String, ScrapeJobEvent> kafkaTemplate;

    @Transactional
    public JobResponse createJob(CreateJobRequest request) {
        JobEntity entity = jobMapper.toEntity(request);
        entity = jobRepository.save(entity);

        ScrapeJobEvent event = jobMapper.toEvent(entity);
        kafkaTemplate.send(TOPIC_JOBS_PENDING, entity.getId().toString(), event);
        log.info("Job created and published to Kafka: jobId={}, url={}", entity.getId(), entity.getUrl());

        return jobMapper.toResponse(entity);
    }

    public JobResponse getJob(UUID id) {
        return jobRepository.findById(id)
                .map(jobMapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Job not found: " + id));
    }

    public List<JobResponse> getAllJobs() {
        return jobRepository.findAll().stream()
                .map(jobMapper::toResponse)
                .toList();
    }

    public List<JobResponse> getJobsByStatus(JobStatus status) {
        return jobRepository.findByStatus(status).stream()
                .map(jobMapper::toResponse)
                .toList();
    }

    @Transactional
    public void cancelJob(UUID id) {
        JobEntity entity = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found: " + id));
        entity.setStatus(JobStatus.FAILED);
        jobRepository.save(entity);
        log.info("Job cancelled: jobId={}", id);
    }
}
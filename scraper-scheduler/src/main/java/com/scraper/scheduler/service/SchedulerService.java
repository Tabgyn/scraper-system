package com.scraper.scheduler.service;

import com.scraper.scheduler.dto.CreateScheduleRequest;
import com.scraper.scheduler.dto.ScheduleResponse;
import com.scraper.scheduler.entity.ScheduleEntity;
import com.scraper.scheduler.job.ScrapeQuartzJob;
import com.scraper.scheduler.repository.ScheduleRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final ScheduleRepository scheduleRepository;
    private final Scheduler quartzScheduler;

    /**
     * On startup, re-register all active schedules from the DB into Quartz.
     */
    @PostConstruct
    public void restoreSchedules() {
        List<ScheduleEntity> active = scheduleRepository.findByActiveTrue();
        active.forEach(schedule -> {
            try {
                registerWithQuartz(schedule);
            } catch (SchedulerException e) {
                log.error("Failed to restore schedule: id={}", schedule.getId(), e);
            }
        });
        log.info("Restored {} active schedules into Quartz", active.size());
    }

    @Transactional
    public ScheduleResponse createSchedule(CreateScheduleRequest request) throws SchedulerException {
        ScheduleEntity entity = ScheduleEntity.builder()
                .url(request.getUrl())
                .jobType(request.getJobType())
                .cronExpression(request.getCronExpression())
                .active(true)
                .maxAttempts(request.getMaxAttempts())
                .extractorConfigId(request.getExtractorConfigId())
                .headers(request.getHeaders())
                .build();

        entity = scheduleRepository.save(entity);
        registerWithQuartz(entity);

        log.info("Schedule created: id={}, url={}, cron={}",
                entity.getId(), entity.getUrl(), entity.getCronExpression());
        return toResponse(entity);
    }

    @Transactional
    public void pauseSchedule(UUID id) throws SchedulerException {
        ScheduleEntity entity = getEntityOrThrow(id);
        quartzScheduler.pauseJob(JobKey.jobKey(id.toString()));
        entity.setActive(false);
        scheduleRepository.save(entity);
        log.info("Schedule paused: id={}", id);
    }

    @Transactional
    public void resumeSchedule(UUID id) throws SchedulerException {
        ScheduleEntity entity = getEntityOrThrow(id);
        quartzScheduler.resumeJob(JobKey.jobKey(id.toString()));
        entity.setActive(true);
        scheduleRepository.save(entity);
        log.info("Schedule resumed: id={}", id);
    }

    @Transactional
    public void deleteSchedule(UUID id) throws SchedulerException {
        getEntityOrThrow(id);
        quartzScheduler.deleteJob(JobKey.jobKey(id.toString()));
        scheduleRepository.deleteById(id);
        log.info("Schedule deleted: id={}", id);
    }

    public List<ScheduleResponse> getAllSchedules() {
        return scheduleRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public ScheduleResponse getSchedule(UUID id) {
        return toResponse(getEntityOrThrow(id));
    }

    private void registerWithQuartz(ScheduleEntity entity) throws SchedulerException {
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("url", entity.getUrl());
        dataMap.put("jobType", entity.getJobType().name());
        dataMap.put("maxAttempts", entity.getMaxAttempts());
        dataMap.put("extractorConfigId",
                entity.getExtractorConfigId() != null ? entity.getExtractorConfigId() : "");

        JobDetail jobDetail = JobBuilder.newJob(ScrapeQuartzJob.class)
                .withIdentity(entity.getId().toString())
                .usingJobData(dataMap)
                .storeDurably()
                .build();

        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger-" + entity.getId())
                .withSchedule(CronScheduleBuilder
                        .cronSchedule(entity.getCronExpression())
                        .withMisfireHandlingInstructionDoNothing())
                .build();

        if (quartzScheduler.checkExists(JobKey.jobKey(entity.getId().toString()))) {
            quartzScheduler.rescheduleJob(
                    TriggerKey.triggerKey("trigger-" + entity.getId()), trigger);
        } else {
            quartzScheduler.scheduleJob(jobDetail, trigger);
        }
    }

    private ScheduleEntity getEntityOrThrow(UUID id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found: " + id));
    }

    private ScheduleResponse toResponse(ScheduleEntity entity) {
        return ScheduleResponse.builder()
                .id(entity.getId())
                .url(entity.getUrl())
                .jobType(entity.getJobType())
                .cronExpression(entity.getCronExpression())
                .active(entity.isActive())
                .maxAttempts(entity.getMaxAttempts())
                .extractorConfigId(entity.getExtractorConfigId())
                .headers(entity.getHeaders())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
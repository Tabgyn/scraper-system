package com.scraper.scheduler.controller;

import com.scraper.scheduler.dto.CreateScheduleRequest;
import com.scraper.scheduler.dto.ScheduleResponse;
import com.scraper.scheduler.service.SchedulerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final SchedulerService schedulerService;

    @PostMapping
    public ResponseEntity<ScheduleResponse> createSchedule(
            @Valid @RequestBody CreateScheduleRequest request) throws SchedulerException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(schedulerService.createSchedule(request));
    }

    @GetMapping
    public ResponseEntity<List<ScheduleResponse>> getAllSchedules() {
        return ResponseEntity.ok(schedulerService.getAllSchedules());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleResponse> getSchedule(@PathVariable UUID id) {
        return ResponseEntity.ok(schedulerService.getSchedule(id));
    }

    @PutMapping("/{id}/pause")
    public ResponseEntity<Void> pauseSchedule(@PathVariable UUID id) throws SchedulerException {
        schedulerService.pauseSchedule(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/resume")
    public ResponseEntity<Void> resumeSchedule(@PathVariable UUID id) throws SchedulerException {
        schedulerService.resumeSchedule(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable UUID id) throws SchedulerException {
        schedulerService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }
}
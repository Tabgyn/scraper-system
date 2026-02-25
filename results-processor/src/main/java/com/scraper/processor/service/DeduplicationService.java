package com.scraper.processor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeduplicationService {

    private static final String DEDUP_KEY_PREFIX = "dedup:job:";
    private static final Duration DEDUP_TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;

    /**
     * Returns true if this jobId has NOT been seen before (safe to process).
     * Returns false if it's a duplicate (skip processing).
     */
    public boolean isNew(UUID jobId) {
        String key = DEDUP_KEY_PREFIX + jobId.toString();
        Boolean isNew = redisTemplate.opsForValue()
                .setIfAbsent(key, "1", DEDUP_TTL);
        boolean result = Boolean.TRUE.equals(isNew);
        if (!result) {
            log.warn("Duplicate result detected, skipping: jobId={}", jobId);
        }
        return result;
    }
}
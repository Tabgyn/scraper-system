package com.scraper.common.model;

import com.scraper.common.enums.ProxyStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProxyConfig {

    private UUID id;
    private String host;
    private int port;
    private String username;
    private String password;
    private ProxyStatus status;
    private int successCount;
    private int failureCount;
    private double successRate;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant lastCheckedAt;
}
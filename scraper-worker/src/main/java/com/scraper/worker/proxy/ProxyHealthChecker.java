package com.scraper.worker.proxy;

import com.scraper.common.enums.ProxyStatus;
import com.scraper.common.model.ProxyConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProxyHealthChecker {

    private static final String HEALTH_CHECK_URL = "https://httpbin.org/ip";

    private final ProxyManager proxyManager;
    private final WebClient.Builder webClientBuilder;

    /**
     * Runs every 5 minutes — rebuilds the proxy pool with health-checked proxies.
     * In production this would query PostgreSQL for the proxy list.
     * For now use a hardcoded stub — replace with your ProxyRepository query.
     */
    @Scheduled(fixedDelay = 300_000)
    public void refreshProxyPool() {
        log.info("Starting proxy pool health check...");

        List<ProxyConfig> candidates = loadProxyCandidates();

        Flux.fromIterable(candidates)
                .flatMap(proxy -> checkProxy(proxy)
                        .map(healthy -> ProxyConfig.builder()
                                .id(proxy.getId())
                                .host(proxy.getHost())
                                .port(proxy.getPort())
                                .username(proxy.getUsername())
                                .password(proxy.getPassword())
                                .successCount(proxy.getSuccessCount())
                                .failureCount(proxy.getFailureCount())
                                .successRate(proxy.getSuccessRate())
                                .status(healthy ? ProxyStatus.ACTIVE : ProxyStatus.UNHEALTHY)
                                .lastCheckedAt(Instant.now())
                                .build()))
                .collectList()
                .subscribe((List<ProxyConfig> checkedProxies) -> {
                    proxyManager.loadProxies(checkedProxies);
                    log.info("Proxy pool refreshed — total active: {}",
                            checkedProxies.stream()
                                    .filter(p -> p.getStatus() == ProxyStatus.ACTIVE)
                                    .count());
                });
    }

    private reactor.core.publisher.Mono<Boolean> checkProxy(ProxyConfig proxy) {
        return webClientBuilder.build()
                .get()
                .uri(HEALTH_CHECK_URL)
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(5))
                .map(response -> response.getStatusCode().is2xxSuccessful())
                .onErrorReturn(false);
    }

    /**
     * Stub — replace it with an actual PostgreSQL query via ProxyRepository.
     */
    private List<ProxyConfig> loadProxyCandidates() {
        return List.of(); // wire in your DB-sourced proxies here
    }
}
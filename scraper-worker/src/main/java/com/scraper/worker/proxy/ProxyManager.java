package com.scraper.worker.proxy;

import com.scraper.common.enums.ProxyStatus;
import com.scraper.common.model.ProxyConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProxyManager {

    private static final String PROXY_BAN_KEY = "proxy:banned:";
    private static final String RATE_LIMIT_KEY = "ratelimit:domain:";
    private static final int RATE_LIMIT_PER_DOMAIN = 10; // requests per second

    private final StringRedisTemplate redisTemplate;

    // In-memory proxy pool — loaded from PostgreSQL at startup via ProxyHealthChecker
    private final CopyOnWriteArrayList<ProxyConfig> proxyPool = new CopyOnWriteArrayList<>();
    private final AtomicInteger roundRobinIndex = new AtomicInteger(0);

    public void loadProxies(List<ProxyConfig> proxies) {
        proxyPool.clear();
        proxyPool.addAll(proxies.stream()
                .filter(p -> p.getStatus() == ProxyStatus.ACTIVE)
                .toList());
        log.info("Loaded {} active proxies into pool", proxyPool.size());
    }

    /**
     * Returns the next healthy proxy using weighted round-robin.
     * Falls back to no-proxy if pool is empty.
     */
    public ProxyConfig nextProxy() {
        if (proxyPool.isEmpty()) {
            log.warn("Proxy pool is empty — proceeding without proxy");
            return null;
        }

        int attempts = 0;
        while (attempts < proxyPool.size()) {
            int index = roundRobinIndex.getAndIncrement() % proxyPool.size();
            ProxyConfig proxy = proxyPool.get(index);

            if (!isBanned(proxy)) {
                return proxy;
            }
            attempts++;
        }

        log.warn("All proxies are banned — proceeding without proxy");
        return null;
    }

    /**
     * Token bucket rate limiter per domain using Redis.
     * Returns true if the request is allowed.
     */
    public boolean isAllowed(String domain) {
        String key = RATE_LIMIT_KEY + domain;
        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(1));
        }

        boolean allowed = count != null && count <= RATE_LIMIT_PER_DOMAIN;
        if (!allowed) {
            log.debug("Rate limit exceeded for domain: {}", domain);
        }
        return allowed;
    }

    public void markBanned(ProxyConfig proxy) {
        String key = PROXY_BAN_KEY + proxy.getHost() + ":" + proxy.getPort();
        redisTemplate.opsForValue().set(key, "banned", Duration.ofMinutes(30));
        log.warn("Proxy marked as banned: {}:{}", proxy.getHost(), proxy.getPort());
    }

    public void markFailed(ProxyConfig proxy) {
        // Remove from in-memory pool on repeated failure
        proxyPool.removeIf(p ->
                p.getHost().equals(proxy.getHost()) && p.getPort() == proxy.getPort());
        log.warn("Proxy removed from pool due to failure: {}:{}", proxy.getHost(), proxy.getPort());
    }

    private boolean isBanned(ProxyConfig proxy) {
        String key = PROXY_BAN_KEY + proxy.getHost() + ":" + proxy.getPort();
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public int poolSize() {
        return proxyPool.size();
    }
}
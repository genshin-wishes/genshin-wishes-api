package com.uf.genshinwishes.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class PublicStatsHealthIndicator implements HealthIndicator {
    @Autowired
    private CachingConfig caching;

    private final String message_key = "Public Stats Service";

    @Override
    public Health health() {
        if (!isRunningServiceA()) {
            return Health.down().withDetail(message_key, "Not loaded").build();
        }
        return Health.up().withDetail(message_key, "Loaded").build();
    }

    private Boolean isRunningServiceA() {
        return caching.isLoaded();
    }
}

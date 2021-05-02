package com.uf.genshinwishes.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableCaching
@EnableScheduling
@Configuration
public class CachingConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("usersCount", "wishesCount");
    }

    @CacheEvict(allEntries = true, cacheNames = { "usersCount", "wishesCount" })
    @Scheduled(fixedDelay = 60000)
    public void cacheEvict() {
    }
}

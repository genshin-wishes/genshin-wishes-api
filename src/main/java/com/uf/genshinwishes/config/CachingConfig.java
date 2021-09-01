package com.uf.genshinwishes.config;

import com.uf.genshinwishes.service.UserService;
import com.uf.genshinwishes.service.WishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
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

    @Autowired
    private WishService wishService;
    @Autowired
    private UserService userService;

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("publicStats", "usersCount", "wishesCount");
    }

    @Scheduled(fixedDelay = 300000, initialDelay = 0)
    public void countersUpdate() {
        this.wishService.updateWishesCount();
        this.userService.updateUsersCount();
    }
}

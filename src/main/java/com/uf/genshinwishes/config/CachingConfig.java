package com.uf.genshinwishes.config;

import com.uf.genshinwishes.dto.BannerDTO;
import com.uf.genshinwishes.model.BannerType;
import com.uf.genshinwishes.service.BannerService;
import com.uf.genshinwishes.service.PublicStatsService;
import com.uf.genshinwishes.service.UserService;
import com.uf.genshinwishes.service.WishService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@EnableCaching
@EnableScheduling
@Configuration
public class CachingConfig {

    @Autowired
    private PublicStatsService publicStatsService;
    @Autowired
    private BannerService bannerService;
    @Autowired
    private WishService wishService;
    @Autowired
    private UserService userService;

    @Getter
    private boolean loaded = false;

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("publicStats", "usersCount", "wishesCount");
    }

    @Scheduled(fixedDelay = 1800000, initialDelay = 0)
    public void publicStatsUpdate() {
        Instant oldWishes = Instant.now().minus(200, ChronoUnit.DAYS);

        publicStatsService.updateStatsFor(BannerType.ALL, null);
        publicStatsService.updateStatsFor(BannerType.CHARACTER_EVENT, null);
        publicStatsService.updateStatsFor(BannerType.WEAPON_EVENT, null);

        List<BannerDTO> banners = bannerService.findAll();

        banners.forEach(b -> {
            if(loaded && b.getEnd() != null && b.getEnd().isBefore(LocalDateTime.ofInstant(oldWishes, ZoneId.of("UTC")))) return;

            publicStatsService.updateStatsFor(
                b.getGachaType(),
                b.getGachaType() == BannerType.CHARACTER_EVENT || b.getGachaType() == BannerType.WEAPON_EVENT
                    ? b.getId()
                    : null);
        });

        loaded = true;
    }

    @Scheduled(fixedDelay = 300000, initialDelay = 0)
    public void countersUpdate() {
        this.wishService.updateWishesCount();
        this.userService.updateUsersCount();
    }
}

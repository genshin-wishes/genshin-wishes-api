package com.uf.genshinwishes.config;

import com.uf.genshinwishes.dto.BannerDTO;
import com.uf.genshinwishes.model.BannerType;
import com.uf.genshinwishes.model.User;
import com.uf.genshinwishes.service.BannerService;
import com.uf.genshinwishes.service.PublicStatsService;
import com.uf.genshinwishes.service.UserService;
import com.uf.genshinwishes.service.WishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Arrays;
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

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("publicStats", "usersCount", "wishesCount");
    }

    @Scheduled(fixedDelay = 900000, initialDelay = 0)
    public void publicStatsUpdate() {
        publicStatsService.updateStatsFor(BannerType.ALL, null);
        publicStatsService.updateStatsFor(BannerType.CHARACTER_EVENT, null);
        publicStatsService.updateStatsFor(BannerType.WEAPON_EVENT, null);

        List<BannerDTO> banners = bannerService.findAll();

        banners.forEach(b -> {
            publicStatsService.updateStatsFor(
                b.getGachaType(),
                b.getGachaType() == BannerType.CHARACTER_EVENT || b.getGachaType() == BannerType.WEAPON_EVENT
                    ? b.getId()
                    : null);
        });
    }

    @Scheduled(fixedDelay = 60000, initialDelay = 0)
    public void countersUpdate() {
        this.wishService.updateWishesCount();
        this.userService.updateUsersCount();
    }
}

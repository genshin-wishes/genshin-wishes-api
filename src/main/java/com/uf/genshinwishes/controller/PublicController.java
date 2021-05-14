package com.uf.genshinwishes.controller;

import com.uf.genshinwishes.dto.BannerDTO;
import com.uf.genshinwishes.dto.PublicStatsDTO;
import com.uf.genshinwishes.dto.WishFilterDTO;
import com.uf.genshinwishes.model.BannerType;
import com.uf.genshinwishes.service.BannerService;
import com.uf.genshinwishes.service.PublicStatsService;
import com.uf.genshinwishes.service.UserService;
import com.uf.genshinwishes.service.WishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/public")
public class PublicController {
    @Autowired
    private UserService userService;
    @Autowired
    private WishService wishService;
    @Autowired
    private BannerService bannerService;
    @Autowired
    private PublicStatsService publicStatsService;

    @GetMapping("/languages")
    public Map<String, String> getLanguages(@RequestParam("locales") List<String> locales) {
        return locales.stream()
            .map(locale -> Locale.forLanguageTag(locale))
            .collect(Collectors.toMap(Locale::toLanguageTag, locale -> locale.getDisplayName(locale)));
    }

    @GetMapping("/banners")
    public List<BannerDTO> getBanners() {
        return bannerService.findAll();
    }

    @GetMapping("/banners/latest")
    public Map<Integer, BannerDTO> getLatestEvents() {
        return bannerService.getLatestBannerToEventMap(null);
    }

    @GetMapping("/users/count")
    public Long getUsersCount() {
        return this.userService.getUsersCount();
    }

    @GetMapping("/wishes/count")
    public Long getWishesCount() {
        return this.wishService.getWishesCount();
    }

    @GetMapping("/stats/{bannerType}")
    public PublicStatsDTO getPublicStats(@PathVariable("bannerType") Optional<BannerType> bannerType,
                                         @RequestParam Optional<Long> event) {
        return this.publicStatsService.getStatsFor(bannerType.orElse(BannerType.ALL), event.orElse(null));
    }
}

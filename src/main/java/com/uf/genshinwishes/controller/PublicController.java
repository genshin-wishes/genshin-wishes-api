package com.uf.genshinwishes.controller;

import com.uf.genshinwishes.dto.BannerDTO;
import com.uf.genshinwishes.model.BannerType;
import com.uf.genshinwishes.service.BannerService;
import com.uf.genshinwishes.service.UserService;
import com.uf.genshinwishes.service.WishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;
import java.util.Map;
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

    @GetMapping("/banners/character")
    public Iterable<BannerDTO> getCharacterEvents() {
        return bannerService.findAllByGachaTypeOrderByStartDateDesc(BannerType.CHARACTER_EVENT.getType());
    }

    @GetMapping("/banners/weapon")
    public Iterable<BannerDTO> getWeaponEvents() {
        return bannerService.findAllByGachaTypeOrderByStartDateDesc(BannerType.WEAPON_EVENT.getType());
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

}

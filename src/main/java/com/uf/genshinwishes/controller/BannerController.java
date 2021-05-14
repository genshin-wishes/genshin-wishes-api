package com.uf.genshinwishes.controller;

import com.uf.genshinwishes.dto.BannerDTO;
import com.uf.genshinwishes.model.BannerType;
import com.uf.genshinwishes.model.User;
import com.uf.genshinwishes.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/banners")
public class BannerController {
    @Autowired
    private BannerService bannerService;

    @GetMapping("")
    public Iterable<BannerDTO> getBanners(User user) {
        return bannerService.findAllForUser(user);
    }

    @GetMapping("/character")
    public Iterable<BannerDTO> getCharacterEvents(User user) {
        return bannerService.findAllByGachaTypeOrderByStartDateDesc(user, BannerType.CHARACTER_EVENT.getType());
    }

    @GetMapping("/weapon")
    public Iterable<BannerDTO> getWeaponEvents(User user) {
        return bannerService.findAllByGachaTypeOrderByStartDateDesc(user, BannerType.WEAPON_EVENT.getType());
    }

    @GetMapping("/latest")
    public Map<Integer, BannerDTO> getLatestEvents(User user) {
        return bannerService.getLatestBannerToEventMap(user);
    }

}

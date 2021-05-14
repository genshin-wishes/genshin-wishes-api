package com.uf.genshinwishes.controller;

import com.uf.genshinwishes.dto.BannerDTO;
import com.uf.genshinwishes.dto.StatsDTO;
import com.uf.genshinwishes.dto.WishFilterDTO;
import com.uf.genshinwishes.exception.ApiError;
import com.uf.genshinwishes.exception.ErrorType;
import com.uf.genshinwishes.model.BannerType;
import com.uf.genshinwishes.model.User;
import com.uf.genshinwishes.service.BannerService;
import com.uf.genshinwishes.service.StatsService;
import com.uf.genshinwishes.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/profile/{profileId}")
public class ProfileController {
    @Autowired
    private UserService userService;
    @Autowired
    private BannerService bannerService;
    @Autowired
    private StatsService statsService;

    @GetMapping("/")
    public String getUsername(@PathVariable("profileId") String profileId) {
        User user = assertUser(profileId);

        return user.getMihoyoUsername();
    }

    @GetMapping("/banners")
    public Iterable<BannerDTO> getBanners(@PathVariable("profileId") String profileId) {
        User user = assertUser(profileId);

        return bannerService.findAllForUser(user);
    }

    @GetMapping("/banners/character")
    public Iterable<BannerDTO> getCharacterEvents(@PathVariable("profileId") String profileId) {
        User user = assertUser(profileId);

        return bannerService.findAllByGachaTypeOrderByStartDateDesc(user, BannerType.CHARACTER_EVENT.getType());
    }

    @GetMapping("/banners/weapon")
    public Iterable<BannerDTO> getWeaponEvents(@PathVariable("profileId") String profileId) {
        User user = assertUser(profileId);

        return bannerService.findAllByGachaTypeOrderByStartDateDesc(user, BannerType.WEAPON_EVENT.getType());
    }

    @GetMapping("/stats/{bannerType}")
    public StatsDTO getStats(@PathVariable("profileId") String profileId,
                             @PathVariable("bannerType") Optional<BannerType> bannerType,
                             @RequestParam Optional<List<Long>> characterEvents,
                             @RequestParam Optional<List<Long>> weaponEvents) {
        User user = assertUser(profileId);

        WishFilterDTO filters = WishFilterDTO.builder().events(
            Stream.concat(
                characterEvents.orElse(Collections.emptyList()).stream(),
                weaponEvents.orElse(Collections.emptyList()).stream()
            ).collect(Collectors.toList())
        ).build();

        return statsService.getStatsFor(user, bannerType.orElse(BannerType.ALL), filters);
    }

    private User assertUser(@PathVariable("profileId") String profileId) {
        User user = this.userService.findUserByProfileId(profileId);

        if (user == null || user.getSharing() == null || !user.getSharing()) {
            throw new ApiError(ErrorType.PROFILE_NOT_FOUND);
        }

        return user;
    }
}

package com.uf.genshinwishes.controller;

import com.uf.genshinwishes.dto.ItemType;
import com.uf.genshinwishes.dto.WishFilterDTO;
import com.uf.genshinwishes.model.BannerType;
import com.uf.genshinwishes.model.User;
import com.uf.genshinwishes.model.Wish;
import com.uf.genshinwishes.service.WishService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/wishes")
public class WishController {
    private final Logger logger = LoggerFactory.getLogger(WishController.class);

    @Autowired
    private WishService wishService;

    @GetMapping("/{bannerType}")
    public List<Wish> getWishes(User user,
                                @PathVariable("bannerType") BannerType bannerType,
                                @RequestParam("page") Integer page,
                                @RequestParam Optional<String> freeText,
                                @RequestParam Optional<Boolean> fr,
                                @RequestParam Optional<List<Integer>> rank,
                                @RequestParam Optional<ItemType> itemType,
                                @RequestParam Optional<Date> startDate,
                                @RequestParam Optional<Date> endDate) {
        return wishService.findByUserAndBannerType(user, bannerType, page, new WishFilterDTO(
            freeText.orElse(null),
            fr.orElse(null),
            rank.orElse(null),
            itemType.orElse(null),
            startDate.orElse(null),
            endDate.orElse(null)
        ));
    }

    @GetMapping("/{bannerType}/count")
    public Long countWishesByBanner(User user, @PathVariable("bannerType") BannerType bannerType,
                                    @RequestParam Optional<String> freeText,
                                    @RequestParam Optional<Boolean> fr,
                                    @RequestParam Optional<List<Integer>> rank,
                                    @RequestParam Optional<ItemType> itemType,
                                    @RequestParam Optional<Date> startDate,
                                    @RequestParam Optional<Date> endDate
    ) {
        WishFilterDTO filters = new WishFilterDTO(
            freeText.orElse(null),
            fr.orElse(null),
            rank.orElse(null),
            itemType.orElse(null),
            startDate.orElse(null),
            endDate.orElse(null)
        );

        return wishService.countAllByUserAndGachaType(user, bannerType, filters);
    }

    @GetMapping("/count")
    public Map<BannerType, Long> countWishes(User user) {
        return wishService.countAllByUser(user);
    }

    @GetMapping("/import")
    public Map<BannerType, Integer> importWishes(User user, @RequestParam("authkey") String authkey) {
        return wishService.importWishes(user, authkey);
    }

    @GetMapping("/banners")
    public Map<BannerType, Collection<Wish>> getBanners(User user) {
        return wishService.getBanners(user);
    }

    @DeleteMapping("/deleteAll")
    public void deleteAll(User user) {
        wishService.deleteAll(user);
    }
}

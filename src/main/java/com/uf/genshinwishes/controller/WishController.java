package com.uf.genshinwishes.controller;

import com.uf.genshinwishes.model.BannerType;
import com.uf.genshinwishes.model.User;
import com.uf.genshinwishes.model.Wish;
import com.uf.genshinwishes.service.WishService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wishes")
public class WishController {
    private final Logger logger = LoggerFactory.getLogger(WishController.class);

    @Autowired
    private WishService wishService;

    @GetMapping("/{bannerType}")
    public List<Wish> getWishes(User user, @PathVariable("bannerType") BannerType bannerType, @RequestParam("page") Integer page) {
        return wishService.findByUserAndBannerType(user, bannerType, page);
    }

    @GetMapping("/{bannerType}/count")
    public Integer countWishesByBanner(User user, @PathVariable("bannerType") BannerType bannerType) {
        return wishService.countAllByUserAndGachaType(user, bannerType);
    }

    @GetMapping("/count")
    public Map<BannerType, Integer> countWishes(User user) {
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

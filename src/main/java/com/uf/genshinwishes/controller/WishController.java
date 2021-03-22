package com.uf.genshinwishes.controller;

import com.uf.genshinwishes.model.BannerType;
import com.uf.genshinwishes.model.User;
import com.uf.genshinwishes.model.Wish;
import com.uf.genshinwishes.dto.ItemType;
import com.uf.genshinwishes.dto.WishDTO;
import com.uf.genshinwishes.dto.WishFilterDTO;
import com.uf.genshinwishes.service.CSVHelper;
import com.uf.genshinwishes.service.WishService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/wishes")
public class WishController {
    private final Logger logger = LoggerFactory.getLogger(WishController.class);

    @Autowired
    private WishService wishService;
    @Autowired
    private MessageSource messageSource;

    @GetMapping("/{bannerType}")
    public List<WishDTO> getWishes(User user,
                                   @PathVariable("bannerType") BannerType bannerType,
                                   @RequestParam("page") Integer page,
                                   @RequestParam Optional<String> freeText,
                                   @RequestParam Optional<Boolean> fr,
                                   @RequestParam Optional<List<Integer>> ranks,
                                   @RequestParam Optional<ItemType> itemType,
                                   @RequestParam Optional<List<Long>> characterEvents,
                                   @RequestParam Optional<List<Long>> weaponEvents) {
        return wishService.findByUserAndBannerType(user, bannerType, page, new WishFilterDTO(
            freeText.orElse(null),
            fr.orElse(null),
            ranks.orElse(null),
            itemType.orElse(null),
            Stream.concat(
                characterEvents.orElse(Collections.emptyList()).stream(),
                weaponEvents.orElse(Collections.emptyList()).stream()
            ).collect(Collectors.toList())
        ));
    }

    @GetMapping("/{bannerType}/count")
    public Long countWishesByBanner(User user, @PathVariable("bannerType") BannerType bannerType,
                                    @RequestParam Optional<String> freeText,
                                    @RequestParam Optional<Boolean> fr,
                                    @RequestParam Optional<List<Integer>> ranks,
                                    @RequestParam Optional<ItemType> itemType,
                                    @RequestParam Optional<List<Long>> characterEvents,
                                    @RequestParam Optional<List<Long>> weaponEvents
    ) {
        WishFilterDTO filters = new WishFilterDTO(
            freeText.orElse(null),
            fr.orElse(null),
            ranks.orElse(null),
            itemType.orElse(null),
            Stream.concat(
                characterEvents.orElse(Collections.emptyList()).stream(),
                weaponEvents.orElse(Collections.emptyList()).stream()
            ).collect(Collectors.toList())
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

    @GetMapping("/export")
    public ResponseEntity<Object> exportWishes(User user) {
        String filename = user.getMihoyoUid() + "_wishes.csv";

        List<Wish> userWishes = wishService.findByUser(user);

        InputStreamResource file = new InputStreamResource(CSVHelper.wishesToCsv(messageSource, user, userWishes));

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.parseMediaType("application/csv"))
            .body(file);
    }

    @GetMapping("/banners")
    public Map<BannerType, Collection<WishDTO>> getBanners(User user) {
        return wishService.getBanners(user);
    }

    @DeleteMapping("/deleteAll")
    public void deleteAll(User user) {
        wishService.deleteAllUserWishes(user);
    }
}

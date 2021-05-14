package com.uf.genshinwishes.controller;

import com.uf.genshinwishes.dto.StatsDTO;
import com.uf.genshinwishes.dto.WishFilterDTO;
import com.uf.genshinwishes.model.BannerType;
import com.uf.genshinwishes.model.User;
import com.uf.genshinwishes.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/stats")
public class StatsController {
    @Autowired
    private StatsService statsService;

    @GetMapping("/{bannerType}")
    public StatsDTO getStats(User user,
                             @PathVariable("bannerType") Optional<BannerType> bannerType,
                             @RequestParam Optional<List<Long>> characterEvents,
                             @RequestParam Optional<List<Long>> weaponEvents) {
        WishFilterDTO filters = WishFilterDTO.builder().events(
            Stream.concat(
                characterEvents.orElse(Collections.emptyList()).stream(),
                weaponEvents.orElse(Collections.emptyList()).stream()
            ).collect(Collectors.toList())
        ).build();

        return statsService.getStatsFor(user, bannerType.orElse(BannerType.ALL), filters);
    }
}

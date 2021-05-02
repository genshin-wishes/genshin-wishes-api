package com.uf.genshinwishes.controller;

import com.uf.genshinwishes.service.UserService;
import com.uf.genshinwishes.service.WishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
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

    @GetMapping("/languages")
    public Map<String, String> getLanguages(@RequestParam("locales") List<String> locales) {
        return locales.stream()
            .map(locale -> Locale.forLanguageTag(locale))
            .collect(Collectors.toMap(Locale::toLanguageTag, locale -> locale.getDisplayName(locale)));
    }

    @GetMapping("/users/count")
    @Cacheable("usersCount")
    public Long getUsersCount() {
        return this.userService.getUsersCount();
    }

    @GetMapping("/wishes/count")
    @Cacheable("wishesCount")
    public Long getWishesCount() {
        return this.wishService.getWishesCount();
    }
}

package com.uf.genshinwishes.controller;

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
    @GetMapping("/languages")
    public Map<String, String> getLanguages(@RequestParam("locales") List<String> locales) {
        return locales.stream()
            .map(locale -> Locale.forLanguageTag(locale))
            .collect(Collectors.toMap(Locale::toLanguageTag, locale -> locale.getDisplayName(locale)));
    }
}

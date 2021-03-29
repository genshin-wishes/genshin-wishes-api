package com.uf.genshinwishes.controller;

import com.uf.genshinwishes.model.Banner;
import com.uf.genshinwishes.model.BannerType;
import com.uf.genshinwishes.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/events")
public class EventController {
    @Autowired
    private EventService eventService;

    @GetMapping("")
    public Iterable<Banner> getEvents() {
        return eventService.findAll();
    }

    @GetMapping("/character")
    public Iterable<Banner> getCharacterEvents() {
        return eventService.findAllByGachaTypeOrderByStartDateDesc(BannerType.CHARACTER_EVENT.getType());
    }

    @GetMapping("/weapon")
    public Iterable<Banner> getWeaponEvents() {
        return eventService.findAllByGachaTypeOrderByStartDateDesc(BannerType.WEAPON_EVENT.getType());
    }

    @GetMapping("/latest")
    public Map<Integer, Banner> getLatestEvents() {
        return eventService.getBannerToEventMap();
    }

}

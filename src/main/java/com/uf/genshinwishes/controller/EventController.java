package com.uf.genshinwishes.controller;

import com.uf.genshinwishes.model.BannerType;
import com.uf.genshinwishes.model.Event;
import com.uf.genshinwishes.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/events")
public class EventController {
    @Autowired
    private EventService eventService;

    @GetMapping("/character")
    public Iterable<Event> getCharacterEvents() {
        return eventService.findAllByGachaTypeOrderByStartDateDesc(BannerType.CHARACTER_EVENT.getType());
    }

    @GetMapping("/weapon")
    public Iterable<Event> getWeaponEvents() {
        return eventService.findAllByGachaTypeOrderByStartDateDesc(BannerType.WEAPON_EVENT.getType());
    }

    @GetMapping("/latest")
    public Map<Integer, Event> getLatestEvents() {
        return eventService.getBannerToEventMap();
    }

}

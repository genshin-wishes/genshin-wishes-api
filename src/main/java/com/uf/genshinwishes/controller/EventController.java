package com.uf.genshinwishes.controller;

import com.uf.genshinwishes.model.Event;
import com.uf.genshinwishes.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events")
public class EventController {
    @Autowired
    private EventRepository eventRepository;

    @GetMapping("")
    public Iterable<Event> getEvents() {
        return eventRepository.findAllByOrderByStartDateDesc();
    }

    @GetMapping("/latest")
    public Event getLatestEvent() {
        return eventRepository.findFirstByOrderByEndDateDesc();
    }

}

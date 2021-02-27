package com.uf.genshinwishes.service;

import com.google.common.collect.Maps;
import com.uf.genshinwishes.model.BannerType;
import com.uf.genshinwishes.model.Event;
import com.uf.genshinwishes.repository.EventRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class EventService {

    private EventRepository eventRepository;

    public Map<Integer, Event> getBannerToEventMap() {
        HashMap<Integer, Event> bannerToEvent = Maps.newHashMap();
        bannerToEvent.put(BannerType.CHARACTER_EVENT.getType(), eventRepository.findFirstByGachaTypeOrderByEndDateDesc(BannerType.CHARACTER_EVENT.getType()));
        bannerToEvent.put(BannerType.WEAPON_EVENT.getType(), eventRepository.findFirstByGachaTypeOrderByEndDateDesc(BannerType.WEAPON_EVENT.getType()));
        return bannerToEvent;
    }

    public List<Event> findAllByGachaTypeOrderByStartDateDesc(Integer gachaType) {
        return eventRepository.findAllByGachaTypeOrderByStartDateDesc(gachaType);
    }
}

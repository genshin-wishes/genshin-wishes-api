package com.uf.genshinwishes.service;

import com.google.common.collect.Maps;
import com.uf.genshinwishes.model.Banner;
import com.uf.genshinwishes.model.BannerType;
import com.uf.genshinwishes.repository.BannerRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class EventService {

    private BannerRepository bannerRepository;

    public Map<Integer, Banner> getBannerToEventMap() {
        HashMap<Integer, Banner> bannerToEvent = Maps.newHashMap();

        BannerType.getBannersExceptAll().forEach(banner -> bannerToEvent.put(banner.getType(), bannerRepository.findFirstByGachaTypeOrderByEndDesc(banner.getType())));

        return bannerToEvent;
    }

    public List<Banner> findAll() {
        return bannerRepository.findAll();
    }

    public List<Banner> findAllByGachaTypeOrderByStartDateDesc(Integer gachaType) {
        return bannerRepository.findAllByGachaTypeOrderByStartDesc(gachaType);
    }
}

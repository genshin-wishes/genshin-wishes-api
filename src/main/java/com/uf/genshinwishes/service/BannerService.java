package com.uf.genshinwishes.service;

import com.google.common.collect.Maps;
import com.uf.genshinwishes.dto.BannerDTO;
import com.uf.genshinwishes.dto.mapper.BannerMapper;
import com.uf.genshinwishes.model.BannerType;
import com.uf.genshinwishes.model.User;
import com.uf.genshinwishes.repository.BannerRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class BannerService {

    private BannerRepository bannerRepository;
    private BannerMapper bannerMapper;

    public Map<Integer, BannerDTO> getBannerToEventMap(User user) {
        HashMap<Integer, BannerDTO> bannerToEvent = Maps.newHashMap();

        BannerType.getBannersExceptAll().forEach(banner ->
            bannerToEvent.put(banner.getType(),
                bannerMapper.toDto(user, bannerRepository.findFirstByGachaTypeOrderByEndDesc(banner.getType()))
            ));

        return bannerToEvent;
    }

    public List<BannerDTO> findAll(User user) {
        return bannerRepository.findAll().stream().map(b -> bannerMapper.toDto(user, b)).collect(Collectors.toList());
    }

    public List<BannerDTO> findAllByGachaTypeOrderByStartDateDesc(User user, Integer gachaType) {
        return bannerRepository.findAllByGachaTypeOrderByStartDesc(gachaType).stream().map(b -> bannerMapper.toDto(user, b)).collect(Collectors.toList());
    }
}

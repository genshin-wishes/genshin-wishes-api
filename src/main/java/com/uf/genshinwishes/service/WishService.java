package com.uf.genshinwishes.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.uf.genshinwishes.dto.BannerDTO;
import com.uf.genshinwishes.dto.WishDTO;
import com.uf.genshinwishes.dto.WishFilterDTO;
import com.uf.genshinwishes.dto.mapper.BannerMapper;
import com.uf.genshinwishes.dto.mapper.WishMapper;
import com.uf.genshinwishes.dto.mihoyo.MihoyoWishLogDTO;
import com.uf.genshinwishes.exception.ApiError;
import com.uf.genshinwishes.exception.ErrorType;
import com.uf.genshinwishes.model.*;
import com.uf.genshinwishes.repository.ItemRepository;
import com.uf.genshinwishes.repository.wish.WishRepository;
import com.uf.genshinwishes.repository.wish.WishSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class WishService {
    Logger logger = LoggerFactory.getLogger(WishService.class);

    @Autowired
    private WishRepository wishRepository;
    @Autowired
    private BannerService bannerService;
    @Autowired
    private WishMapper wishMapper;

    public Map<BannerType, Collection<WishDTO>> getBanners(User user) {
        Multimap<BannerType, WishDTO> wishesByBanner = MultimapBuilder.hashKeys(BannerType.values().length).arrayListValues().build();

        Arrays.stream(BannerType.values())
            .forEach(type -> {
                List<Wish> wishes = wishRepository.findFirst100ByUserAndGachaTypeOrderByIdDesc(user, type.getType());

                wishesByBanner.putAll(type, wishes.stream().map(wishMapper::toDto).collect(Collectors.toList()));
            });

        return wishesByBanner.asMap();
    }

    @Transactional
    public void deleteAllUserWishes(User user) {
        wishRepository.deleteByUser(user);
    }

    public List<WishDTO> findByUserAndBannerType(User user, BannerType bannerType, Integer page, WishFilterDTO filters) {
        List<BannerDTO> banners = bannerService.findAllForUser(user);
        List<Wish> wishes = this.wishRepository.findAll(
            WishSpecification.builder().user(user).bannerType(bannerType).banners(banners).filters(filters).build(),
            PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "time", "id"))
        ).getContent();

        return wishes.stream().map(wishMapper::toDto).collect(Collectors.toList());
    }

    public Long countAllByUserAndGachaType(User user, BannerType bannerType, WishFilterDTO filters) {
        List<BannerDTO> banners = bannerService.findAllForUser(user);
        return this.wishRepository.count(WishSpecification.builder().user(user).bannerType(bannerType).banners(banners).filters(filters).build());
    }

    public Map<BannerType, Long> countAllByUser(User user) {
        return Arrays.stream(BannerType.values())
            .collect(Collectors.toMap(
                (banner) -> banner,
                (banner) -> this.wishRepository.countByUserAndGachaType(user, banner.getType())
            ));
    }

    public List<Wish> findByUser(User user) {
        return this.wishRepository.findByUserOrderByGachaTypeAscIndexAsc(user);
    }

    @Cacheable("wishesCount")
    public Long getWishesCount() {
        return null;
    }

    @CachePut("wishesCount")
    public Long updateWishesCount() {
        return wishRepository.count();
    }
}

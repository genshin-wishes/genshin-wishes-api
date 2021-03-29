package com.uf.genshinwishes.service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.uf.genshinwishes.dto.BannerDTO;
import com.uf.genshinwishes.dto.WishDTO;
import com.uf.genshinwishes.dto.WishFilterDTO;
import com.uf.genshinwishes.dto.mapper.WishMapper;
import com.uf.genshinwishes.dto.mihoyo.MihoyoUserDTO;
import com.uf.genshinwishes.dto.mihoyo.MihoyoWishLogDTO;
import com.uf.genshinwishes.exception.ApiError;
import com.uf.genshinwishes.exception.ErrorType;
import com.uf.genshinwishes.model.BannerType;
import com.uf.genshinwishes.model.Item;
import com.uf.genshinwishes.model.User;
import com.uf.genshinwishes.model.Wish;
import com.uf.genshinwishes.repository.ItemRepository;
import com.uf.genshinwishes.repository.wish.WishRepository;
import com.uf.genshinwishes.repository.wish.WishSpecification;
import com.uf.genshinwishes.service.mihoyo.MihoyoImRestClient;
import com.uf.genshinwishes.service.mihoyo.MihoyoRestClient;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class WishService {

    private WishRepository wishRepository;
    private BannerService bannerService;
    private ItemRepository itemRepository;
    private MihoyoRestClient mihoyoRestClient;
    private MihoyoImRestClient mihoyoImRestClient;
    private WishMapper wishMapper;
    private EntityManager em;

    @Transactional()
    public Map<BannerType, Integer> importWishes(User user, String authkey) {
        if (Strings.isNullOrEmpty(user.getMihoyoUid())) {
            throw new ApiError(ErrorType.NO_MIHOYO_LINKED);
        }

        MihoyoUserDTO mihoyoUser = mihoyoImRestClient.getUserInfo(Optional.of(user), authkey);

        if (!user.getMihoyoUid().equals(mihoyoUser.getUser_id()))
            throw new ApiError(ErrorType.MIHOYO_UID_DIFFERENT);

        Optional<Wish> ifLastWish = wishRepository.findFirstByUserOrderByTimeDescIdDesc(user);
        Optional<LocalDateTime> ifLastWishDate = ifLastWish.map(Wish::getTime);
        Map<BannerType, Long> oldCounts = countAllByUser(user);
        Map<BannerType, Integer> counts = Maps.newHashMap();

        em.lock(user, LockModeType.OPTIMISTIC_FORCE_INCREMENT);

        List<Wish> bannerWishes = Lists.newArrayList();

        BannerType.getBannersExceptAll().forEach(type -> {
            // Attach user to wishes
            List<Wish> wishes = paginateWishesOlderThanDate(authkey, user.getMihoyoUid(), type, ifLastWishDate);
            // Most recent = highest ID
            Collections.reverse(wishes);

            wishes = wishes.stream().map((wish) -> {
                long index = oldCounts.getOrDefault(type, 0L) + 1;

                wish.setUser(user);
                wish.setIndex(index);

                oldCounts.put(type, index);

                return wish;
            }).collect(Collectors.toList());

            counts.put(type, wishes.size());

            supplyItemId(wishes);

            bannerWishes.addAll(wishes);
        });

        wishRepository.saveAll(bannerWishes);

        return counts;
    }


    private void supplyItemId(List<Wish> wishes) {
        List<Item> items = itemRepository.findAll();

        wishes.forEach(wish -> {
            Item item = items.stream()
                .filter(i -> i.getName().equals(wish.getItemName()))
                .findFirst()
                .orElse(null);

            wish.setItem(item);
        });
    }

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

    private List<Wish> paginateWishesOlderThanDate(String authkey, String uid, BannerType bannerType, Optional<LocalDateTime> ifLastWishDate) {
        List<Wish> wishes = Lists.newLinkedList();
        List<MihoyoWishLogDTO> pageWishes;
        String lastWishId = null;
        int currentPage = 1;

        while (!(pageWishes = getWishesForPage(authkey, bannerType, lastWishId, currentPage++)).isEmpty()) {
            List<Wish> internalWishes = pageWishes.stream()
                .map(wish -> wishMapper.fromMihoyo(wish, uid))
                .collect(Collectors.toList());

            // We got a wish that's older than the last import
            if (ifLastWishDate.isPresent()) {
                if (internalWishes.get(internalWishes.size() - 1).getTime().compareTo(ifLastWishDate.get()) <= 0) {

                    wishes.addAll(pageWishes.stream()
                        .map(wish -> wishMapper.fromMihoyo(wish, uid))
                        .filter(wish -> wish.getTime().isAfter(ifLastWishDate.get()))
                        .collect(Collectors.toList()));

                    break;
                }
            }

            lastWishId = pageWishes.get(pageWishes.size() - 1).getId();
            wishes.addAll(internalWishes);
        }

        if (!wishes.isEmpty()) {
            Wish firstWish = wishMapper.fromMihoyo(getWishesForPage(authkey, bannerType, null, 1).get(0), uid);

            if(!wishes.get(0).getTime().equals(firstWish.getTime())) {
                throw new ApiError(ErrorType.NEW_WISHES_DURING_IMPORT);
            }
        }

        return wishes;
    }

    private List<MihoyoWishLogDTO> getWishesForPage(String authkey, BannerType bannerType, String lastWishId, Integer page) {
        return mihoyoRestClient.getWishes(authkey, bannerType, lastWishId, page);
    }

    public List<WishDTO> findByUserAndBannerType(User user, BannerType bannerType, Integer page, WishFilterDTO filters) {
        List<BannerDTO> banners = bannerService.findAll(user);
        List<Wish> wishes = this.wishRepository.findAll(
            WishSpecification.builder().user(user).bannerType(bannerType).banners(banners).filters(filters).build(),
            PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "time", "id"))
        ).getContent();

        return wishes.stream().map(wishMapper::toDto).collect(Collectors.toList());
    }

    public Long countAllByUserAndGachaType(User user, BannerType bannerType, WishFilterDTO filters) {
        List<BannerDTO> banners = bannerService.findAll(user);
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
}

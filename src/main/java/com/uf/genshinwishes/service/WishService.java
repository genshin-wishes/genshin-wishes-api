package com.uf.genshinwishes.service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.uf.genshinwishes.dto.WishDTO;
import com.uf.genshinwishes.dto.WishFilterDTO;
import com.uf.genshinwishes.dto.mapper.WishMapper;
import com.uf.genshinwishes.dto.mihoyo.MihoyoUserDTO;
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

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class WishService {

    private WishRepository wishRepository;
    private ItemRepository itemRepository;
    private MihoyoRestClient mihoyoRestClient;
    private MihoyoImRestClient mihoyoImRestClient;
    private WishMapper wishMapper;

    @Transactional
    public Map<BannerType, Integer> importWishes(User user, String authkey) {
        if (Strings.isNullOrEmpty(user.getMihoyoUid())) {
            throw new ApiError(ErrorType.NO_MIHOYO_LINKED);
        }

        MihoyoUserDTO mihoyoUser = mihoyoImRestClient.getUserInfo(authkey);

        if (!user.getMihoyoUid().equals(mihoyoUser.getUser_id()))
            throw new ApiError(ErrorType.MIHOYO_UID_DIFFERENT);

        Optional<Wish> ifLastWish = wishRepository.findFirstByUserOrderByTimeDescIdDesc(user);
        Optional<Date> ifLastWishDate = ifLastWish.map(Wish::getTime);
        Map<BannerType, Long> oldCounts = countAllByUser(user);
        Map<BannerType, Integer> counts = Maps.newHashMap();

        Arrays.stream(BannerType.values()).filter(banner -> banner.getType() > 0).forEach(type -> {
            // Attach user to wishes
            List<Wish> wishes = paginateWishesOlderThanDate(authkey, type, ifLastWishDate);

            // Most recent = highest ID
            Collections.reverse(wishes);

            wishes = wishes.stream().map((wish) -> {
                long index = oldCounts.getOrDefault(type, 0l) + 1;

                wish.setUser(user);
                wish.setIndex(index);

                oldCounts.put(type, index);

                return wish;
            }).collect(Collectors.toList());

            counts.put(type, wishes.size());

            supplyItemId(wishes);

            wishRepository.saveAll(wishes);
        });

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
    public void deleteAll(User user) {
        wishRepository.deleteByUser(user);
    }

    private List<Wish> paginateWishesOlderThanDate(String authkey, BannerType bannerType, Optional<Date> ifLastWishDate) {
        List<Wish> wishes = Lists.newLinkedList();
        List<Wish> pageWishes;
        Integer currentPage = 1;

        while (!(pageWishes = getWishesForPage(authkey, bannerType, currentPage++)).isEmpty()) {
            // We got a wish that's older than the last import
            if (ifLastWishDate.isPresent()) {
                if (pageWishes.get(pageWishes.size() - 1).getTime().before(ifLastWishDate.get())
                    || pageWishes.get(pageWishes.size() - 1).getTime().equals(ifLastWishDate.get())) {
                    wishes.addAll(pageWishes.stream()
                        .filter(wish -> wish.getTime().after(ifLastWishDate.get()))
                        .collect(Collectors.toList()));

                    break;
                }
            }

            wishes.addAll(pageWishes);
        }

        return wishes;
    }

    private List<Wish> getWishesForPage(String authkey, BannerType bannerType, Integer page) {
        return mihoyoRestClient.getWishes(authkey, bannerType, page)
            .stream()
            .map(wishMapper::fromMihoyo)
            .collect(Collectors.toList());
    }

    public List<WishDTO> findByUserAndBannerType(User user, BannerType bannerType, Integer page, WishFilterDTO filters) {
        return this.wishRepository.findAll(
            new WishSpecification(user, bannerType, filters),
            PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "time", "id"))
        ).getContent().stream().map(wishMapper::toDto).collect(Collectors.toList());
    }

    public Long countAllByUserAndGachaType(User user, BannerType bannerType, WishFilterDTO filters) {
        return this.wishRepository.count(new WishSpecification(user, bannerType, filters));
    }

    public Map<BannerType, Long> countAllByUser(User user) {
        return Arrays.stream(BannerType.values())
            .collect(Collectors.toMap(
                (banner) -> banner,
                (banner) -> this.wishRepository.countByUserAndGachaType(user, banner.getType())
            ));
    }
}

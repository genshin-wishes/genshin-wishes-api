package com.uf.genshinwishes.service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.uf.genshinwishes.dto.mihoyo.MihoyoListDataDTO;
import com.uf.genshinwishes.dto.mihoyo.MihoyoListRetDTO;
import com.uf.genshinwishes.dto.mihoyo.MihoyoUserDTO;
import com.uf.genshinwishes.dto.mihoyo.MihoyoWishLogDTO;
import com.uf.genshinwishes.exception.ApiError;
import com.uf.genshinwishes.exception.ErrorType;
import com.uf.genshinwishes.model.BannerType;
import com.uf.genshinwishes.model.Item;
import com.uf.genshinwishes.model.User;
import com.uf.genshinwishes.model.Wish;
import com.uf.genshinwishes.repository.WishRepository;
import com.uf.genshinwishes.service.mihoyo.MihoyoImRestClient;
import com.uf.genshinwishes.service.mihoyo.MihoyoRestClient;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class WishService {

    private WishRepository wishRepository;
    private MihoyoRestClient mihoyoRestClient;
    private MihoyoImRestClient mihoyoImRestClient;

    @Transactional
    public Map<BannerType, Integer> importWishes(User user, String authkey) {
        if (Strings.isNullOrEmpty(user.getMihoyoUid())) {
            throw new ApiError(ErrorType.NO_MIHOYO_LINKED);
        }

        MihoyoUserDTO mihoyoUser = mihoyoImRestClient.getUserInfo(authkey);

        if (!user.getMihoyoUid().equals(mihoyoUser.getMihoyoUid()))
            throw new ApiError(ErrorType.MIHOYO_UID_DIFFERENT);

        Optional<Date> ifLastWishDate = wishRepository.findFirstByUserOrderByTimeDescIdDesc(user).map(Wish::getTime);

        Map<BannerType, Integer> counts = Maps.newHashMap();

        Arrays.stream(BannerType.values()).forEach(type -> {
            List<MihoyoWishLogDTO> wishes = paginateWishesOlderThanDate(authkey, type, ifLastWishDate);
            List<Wish> newWishes = wishes.stream()
                .map(wishLogDTO -> {
                    Item item = new Item();
                    item.setItemId(Long.parseLong(wishLogDTO.getItem_id()));

                    Wish wish = new Wish();

                    wish.setUid(wishLogDTO.getUid());
                    wish.setGachaType(type.getType());
                    wish.setItem(item);
                    wish.setTime(wishLogDTO.getTime());
                    wish.setUser(user);

                    return wish;
                })
                .collect(Collectors.toList());

            counts.put(type, newWishes.size());

            // Most recent = highest ID
            Collections.reverse(newWishes);

            wishRepository.saveAll(newWishes);
        });

        return counts;
    }

    public Map<BannerType, Collection<Wish>> getBanners(User user) {
        Multimap<BannerType, Wish> wishesByBanner = MultimapBuilder.hashKeys(BannerType.values().length).arrayListValues().build();
        ;

        Arrays.stream(BannerType.values())
            .forEach(type -> wishesByBanner.putAll(type, wishRepository.findFirst100ByUserAndGachaTypeOrderByIdDesc(user, type.getType())));

        return wishesByBanner.asMap();
    }

    @Transactional
    public void deleteAll(User user) {
        wishRepository.deleteByUser(user);
    }

    private List<MihoyoWishLogDTO> paginateWishesOlderThanDate(String authkey, BannerType bannerType, Optional<Date> ifLastWishDate) {
        List<MihoyoWishLogDTO> wishes = Lists.newLinkedList();
        List<MihoyoWishLogDTO> pageWishes;
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

    private List<MihoyoWishLogDTO> getWishesForPage(String authkey, BannerType bannerType, Integer page) {
        MihoyoListRetDTO ret = mihoyoRestClient.getWishes(authkey, bannerType, page).getBody();

        if (ret.getRetcode() == -1 || !(ret.getData() instanceof MihoyoListDataDTO)) {
            throw new ApiError(ErrorType.AUTHKEY_INVALID);
        }

        MihoyoListDataDTO<MihoyoWishLogDTO> listData = (MihoyoListDataDTO) ret.getData();

        if (listData.getList().isEmpty()) {
            return Collections.emptyList();
        }

        return listData.getList().stream().map(one -> {
            MihoyoWishLogDTO dto = new MihoyoWishLogDTO();
            dto.setUid(one.get("uid"));
            dto.setGacha_type(Integer.parseInt(one.get("gacha_type")));
            dto.setTime(
                Date.from(
                    Instant.from(
                        DateTimeFormatter.ISO_INSTANT.parse(
                            one.get("time").replace(' ', 'T').concat("Z")
                        )
                    )
                )
            );
            dto.setItem_id(one.get("item_id"));
            return dto;
        }).collect(Collectors.toList());
    }

    public List<Wish> findByUserAndBannerType(User user, BannerType bannerType, Integer page) {
        return this.wishRepository.findAllByUserAndGachaTypeOrderByIdDesc(
            PageRequest.of(page, 10),
            user,
            bannerType.getType())
            .getContent();
    }

    public Integer countAllByUserAndGachaType(User user, BannerType bannerType) {
        return this.wishRepository.countAllByUserAndGachaType(user, bannerType.getType());
    }

    public Map<BannerType, Integer> countAllByUser(User user) {
        return Arrays.stream(BannerType.values())
            .collect(Collectors.toMap(
                (banner) -> banner,
                (banner) -> this.wishRepository.countByUserAndGachaType(user, banner.getType())
            ));
    }
}

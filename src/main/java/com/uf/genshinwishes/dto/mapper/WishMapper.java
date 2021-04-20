package com.uf.genshinwishes.dto.mapper;

import com.uf.genshinwishes.dto.WishDTO;
import com.uf.genshinwishes.dto.mihoyo.MihoyoWishLogDTO;
import com.uf.genshinwishes.exception.ApiError;
import com.uf.genshinwishes.exception.ErrorType;
import com.uf.genshinwishes.model.Item;
import com.uf.genshinwishes.model.Wish;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class WishMapper {
    public WishDTO toDto(Wish wish) {
        if (wish == null) return null;

        WishDTO wishDTO = new WishDTO();

        wishDTO.setTime(wish.getTime());
        wishDTO.setGachaType(wish.getGachaType());
        wishDTO.setIndex(wish.getIndex());
        wishDTO.setPity(wish.getPity());

        if (wish.getItem() != null) {
            wishDTO.setItemId(wish.getItem().getItemId());
        }

        return wishDTO;
    }

    public Wish fromMihoyo(MihoyoWishLogDTO mihoyoWish, List<Item> items) {
        if (mihoyoWish == null) return null;

        Wish wish = new Wish();

        Item item = items.stream()
            .filter(i -> i.getName().equals(mihoyoWish.getName()))
            .findFirst()
            .orElse(null);

        if (item == null) throw new ApiError(ErrorType.MISSING_ITEM);

        wish.setItem(item);

        wish.setGachaType(mihoyoWish.getGacha_type());
        wish.setTime(
            LocalDateTime.from(
                DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(
                    mihoyoWish.getTime().replace(' ', 'T')
                )
            )
        );

        return wish;
    }
}

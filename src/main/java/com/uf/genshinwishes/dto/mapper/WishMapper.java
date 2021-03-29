package com.uf.genshinwishes.dto.mapper;

import com.uf.genshinwishes.dto.WishDTO;
import com.uf.genshinwishes.dto.mihoyo.MihoyoWishLogDTO;
import com.uf.genshinwishes.model.Item;
import com.uf.genshinwishes.model.Wish;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class WishMapper {
    private final Logger logger = LoggerFactory.getLogger(WishMapper.class);

    public WishDTO toDto(Wish wish) {
        if (wish == null) return null;

        WishDTO wishDTO = new WishDTO();

        wishDTO.setTime(wish.getTime());
        wishDTO.setGachaType(wish.getGachaType());
        wishDTO.setIndex(wish.getIndex());

        if (wish.getItem() != null) {
            wishDTO.setItemId(wish.getItem().getItemId());
        }

        if (wish.getBanner() != null) {
            wishDTO.setBannerId(wish.getBanner().getId());
        }


        return wishDTO;
    }

    public Wish fromMihoyo(MihoyoWishLogDTO mihoyoWish, String uid) {
        if (mihoyoWish == null) return null;

        Wish wish = new Wish();

        if (mihoyoWish.getItem_id() != null && !"".equals(mihoyoWish.getItem_id())) {
            Item item = new Item();
            item.setItemId(Long.parseLong(mihoyoWish.getItem_id()));
            wish.setItem(item);
        }

        wish.setUid(mihoyoWish.getUid());
        wish.setItemName(mihoyoWish.getName());
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

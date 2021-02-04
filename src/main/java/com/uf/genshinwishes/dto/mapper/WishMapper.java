package com.uf.genshinwishes.dto.mapper;

import com.uf.genshinwishes.dto.mihoyo.MihoyoWishLogDTO;
import com.uf.genshinwishes.model.Item;
import com.uf.genshinwishes.model.Wish;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Component
public class WishMapper {

    public Wish fromMihoyo(MihoyoWishLogDTO mihoyoWish) {
        Wish wish = new Wish();

        if(mihoyoWish.getItem_id() != null && !"".equals(mihoyoWish.getItem_id())) {
            Item item = new Item();
            item.setItemId(Long.parseLong(mihoyoWish.getItem_id()));
            wish.setItem(item);
        }

        wish.setUid(mihoyoWish.getUid());
        wish.setItemName(mihoyoWish.getName());
        wish.setGachaType(mihoyoWish.getGacha_type());
        wish.setTime(
            Date.from(
                Instant.from(
                    DateTimeFormatter.ISO_INSTANT.parse(
                        mihoyoWish.getTime().replace(' ', 'T').concat("Z")
                    )
                )
            )
        );

        return wish;
    }
}

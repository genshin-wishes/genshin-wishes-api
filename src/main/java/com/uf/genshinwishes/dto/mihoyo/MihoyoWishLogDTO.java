package com.uf.genshinwishes.dto.mihoyo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MihoyoWishLogDTO {
    private String uid;
    private String item_id;
    private Integer gacha_type;
    private String time;
}

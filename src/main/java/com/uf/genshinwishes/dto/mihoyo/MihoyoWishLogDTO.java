package com.uf.genshinwishes.dto.mihoyo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MihoyoWishLogDTO {
    private String id;
    private String uid;
    private String item_id;
    private String name;
    private Integer gacha_type;
    private String time;
}

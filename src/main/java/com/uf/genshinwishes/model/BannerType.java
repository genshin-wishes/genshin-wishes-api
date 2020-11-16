package com.uf.genshinwishes.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BannerType {
    NOVICE(100),
    PERMANENT(200),
    CHARACTER_EVENT(301),
    WEAPON_EVENT(302),

    //
    ;

    private Integer type;
}

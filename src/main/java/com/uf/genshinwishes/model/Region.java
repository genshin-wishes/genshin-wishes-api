package com.uf.genshinwishes.model;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Region {
    AMERICA("6"),
    EUROPE("7"),
    ASIA("8"),

    //
    ;

    private String prefix;

    Region(String prefix) {
        this.prefix = prefix;
    }

    public static Region getFromUser(User user) {
        return Arrays.stream(Region.values())
            .filter(r -> r.getPrefix().equals(user.getMihoyoUid().charAt(0)))
            .findFirst().orElse(Region.ASIA);
    }
}

package com.uf.genshinwishes.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WishFilterDTO {
    private String freeText;
    private Boolean fr;
    private List<Integer> ranks;
    private ItemType itemType;
    private List<Long> events;
}

package com.uf.genshinwishes.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WishFilterDTO {
    private String freeText;
    private Boolean fr;
    private List<Integer> ranks;
    private ItemType itemType;
    private List<Long> events;
}

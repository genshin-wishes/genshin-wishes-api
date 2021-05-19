package com.uf.genshinwishes.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder=true)
@ToString
public class WishFilterDTO {
    private List<Long> items;
    private Boolean fr;
    private List<Integer> ranks;
    private ItemType itemType;
    private List<Long> events;
}

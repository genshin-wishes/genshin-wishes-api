package com.uf.genshinwishes.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder=true)
public class WishFilterDTO {
    private String freeText;
    private Boolean fr;
    private List<Integer> ranks;
    private ItemType itemType;
    private List<Long> events;
}

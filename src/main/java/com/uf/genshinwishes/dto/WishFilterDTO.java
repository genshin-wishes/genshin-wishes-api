package com.uf.genshinwishes.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WishFilterDTO {
    private String freeText;
    private Boolean fr;
    private List<Integer> rank;
    private ItemType itemType;
    private Date startDate;
    private Date endDate;
}

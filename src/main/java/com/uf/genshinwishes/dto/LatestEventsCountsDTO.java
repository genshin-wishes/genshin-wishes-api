package com.uf.genshinwishes.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class LatestEventsCountsDTO {
    private Long count;
    private Long count5;
    private Long count4;
    private List<CountPerItemId> items;
}

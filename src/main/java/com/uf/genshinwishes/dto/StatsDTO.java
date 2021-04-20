package com.uf.genshinwishes.dto;

import com.uf.genshinwishes.model.BannerType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatsDTO {
    private BannerType bannerType;

    private Long count;
    private Long count4Stars;
    private Long count5Stars;

    private List<WishDTO> wishes;

    private List<CountPerDayDTO> countPerDay;
}

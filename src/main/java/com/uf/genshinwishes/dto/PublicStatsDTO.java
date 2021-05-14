package com.uf.genshinwishes.dto;

import com.uf.genshinwishes.model.BannerType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class PublicStatsDTO {
    private Long count;
    private Long count5Stars;
    private Long count4Stars;

    private Float exclusiveRate5Stars;
    private Float exclusiveRate4Stars;

    private Map<BannerType, LatestEventsCountsDTO> latestEventsCounts;

    private List<CountPerRegion> usersPerRegion;
    private List<CountPerRegion> countPerRegion;

    private List<CountPerBanner> countPerBanner;

    private List<CountPerPity> countPerPity5Stars;
    private List<CountPerPity> countPerPity4Stars;

    private List<CountPerDay> countPerDay;
    private List<CountPerItemId> countPerItemId;
}

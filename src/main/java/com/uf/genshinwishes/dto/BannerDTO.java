package com.uf.genshinwishes.dto;

import com.uf.genshinwishes.model.BannerType;
import com.uf.genshinwishes.model.Image;
import com.uf.genshinwishes.model.Item;
import com.uf.genshinwishes.model.Region;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BannerDTO {
    private Long id;
    private String version;
    private List<Item> items;
    LocalDateTime start;
    LocalDateTime end;
    Map<Region, LocalDateTime[]> startEndByRegion;
    private BannerType gachaType;
    private Image image;
}

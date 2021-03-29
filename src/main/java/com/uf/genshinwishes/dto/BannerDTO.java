package com.uf.genshinwishes.dto;

import com.uf.genshinwishes.model.BannerType;
import com.uf.genshinwishes.model.Image;
import com.uf.genshinwishes.model.Item;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BannerDTO {
    private Long id;
    private List<Item> items;
    private LocalDateTime start;
    private LocalDateTime end;
    private BannerType gachaType;
    private Image image;
}

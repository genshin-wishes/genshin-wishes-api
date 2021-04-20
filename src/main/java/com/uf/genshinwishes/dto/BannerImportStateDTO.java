package com.uf.genshinwishes.dto;

import com.uf.genshinwishes.model.BannerType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BannerImportStateDTO {
    private BannerType bannerType;
    private Integer count;
    private boolean finished;
    private boolean saved;
    private String error;
}

package com.uf.genshinwishes.dto.mapper;

import com.uf.genshinwishes.dto.BannerDTO;
import com.uf.genshinwishes.model.Banner;
import com.uf.genshinwishes.model.BannerType;
import com.uf.genshinwishes.model.User;
import com.uf.genshinwishes.service.UserService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class BannerMapper {
    public BannerDTO toDto(User user, Banner banner) {
        if (banner == null) return null;

        BannerDTO bannerDTO = new BannerDTO();

        bannerDTO.setId(banner.getId());
        bannerDTO.setItems(banner.getItems());
        bannerDTO.setGachaType(BannerType.from(banner.getGachaType()).orElse(null));
        bannerDTO.setImage(banner.getImage());

        if (banner.getStart() != null)
            bannerDTO.setStart(computeDate(user, banner.getStart(), banner.getIsStartLocale()));

        if (banner.getEnd() != null)
            bannerDTO.setEnd(computeDate(user, banner.getEnd(), banner.getIsEndLocale()));

        return bannerDTO;
    }

    private LocalDateTime computeDate(User user, LocalDateTime date, Boolean isLocale) {
        return isLocale != null && isLocale ? date : date.minusHours(UserService.getRegionOffset(user));
    }
}

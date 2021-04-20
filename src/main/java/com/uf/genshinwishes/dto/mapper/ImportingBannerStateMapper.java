package com.uf.genshinwishes.dto.mapper;

import com.uf.genshinwishes.dto.BannerImportStateDTO;
import com.uf.genshinwishes.model.BannerType;
import com.uf.genshinwishes.model.ImportingBannerState;
import org.springframework.stereotype.Component;

@Component
public class ImportingBannerStateMapper {
    public BannerImportStateDTO toDto(ImportingBannerState bannerState) {
        if (bannerState == null) return null;

        BannerImportStateDTO importStateDTO = new BannerImportStateDTO();

        importStateDTO.setBannerType(BannerType.from(bannerState.getGachaType()).orElse(null));
        importStateDTO.setCount(bannerState.getCount());
        importStateDTO.setFinished(bannerState.getFinished());
        importStateDTO.setSaved(bannerState.getSaved());
        importStateDTO.setError(bannerState.getError());

        return importStateDTO;
    }
}

package com.uf.genshinwishes.service.mihoyo;

import com.uf.genshinwishes.dto.mihoyo.MihoyoListRetDTO;
import com.uf.genshinwishes.exception.ApiError;
import com.uf.genshinwishes.exception.ErrorType;
import com.uf.genshinwishes.model.BannerType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class MihoyoRestClient {
    @Value("${app.mihoyo.endpoint}")
    private String mihoyoEndpoint;

    public ResponseEntity<MihoyoListRetDTO> getWishes(String authkey, BannerType banner, Integer page) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(mihoyoEndpoint + "/event/gacha_info/api/getGachaLog")
            .queryParam("authkey", authkey)
            .queryParam("init_type", banner.getType())
            .queryParam("gacha_type", banner.getType())
            .queryParam("authkey_ver", 1)
            .queryParam("sign_type", 2)
            .queryParam("auth_appid", "webview_gacha")
            .queryParam("lang", "fr")
            .queryParam("size", 20)
            .queryParam("page", page);


        try {
            return new RestTemplate().getForEntity(builder.build(true).toUri(), MihoyoListRetDTO.class);
        } catch (Exception e) {
            throw new ApiError(ErrorType.MIHOYO_UNREACHABLE);
        }
    }

}

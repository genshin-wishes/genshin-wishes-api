package com.uf.genshinwishes.service.mihoyo;

import com.uf.genshinwishes.dto.mihoyo.MihoyoWishLogDTO;
import com.uf.genshinwishes.dto.mihoyo.MihoyoWishRetDTO;
import com.uf.genshinwishes.exception.ApiError;
import com.uf.genshinwishes.exception.ErrorType;
import com.uf.genshinwishes.model.BannerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
public class MihoyoRestClient {
    private final Logger logger = LoggerFactory.getLogger(MihoyoRestClient.class);

    @Autowired
    private RestTemplate restTemplate;

    private String mihoyoEndpoint;

    MihoyoRestClient(@Value("${app.mihoyo.endpoint}") String mihoyoEndpoint) {
        this.mihoyoEndpoint = mihoyoEndpoint;
    }

    public List<MihoyoWishLogDTO> getWishes(String authkey, BannerType banner, Integer page) throws ApiError {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(mihoyoEndpoint + "/event/gacha_info/api/getGachaLog")
            .queryParam("authkey", authkey)
            .queryParam("init_type", banner.getType())
            .queryParam("gacha_type", banner.getType())
            .queryParam("authkey_ver", 1)
            .queryParam("sign_type", 2)
            .queryParam("auth_appid", "webview_gacha")
            .queryParam("lang", "en")
            .queryParam("size", 20)
            .queryParam("page", page);

        MihoyoWishRetDTO ret;

        try {
           ret = restTemplate.getForEntity(builder.build(true).toUri(), MihoyoWishRetDTO.class).getBody();
        } catch (Exception e) {
            logger.error("Can't import wishes from mihoyo", e);
            throw new ApiError(ErrorType.MIHOYO_UNREACHABLE);
        }

        if (ret.getRetcode() == -1 || ret.getData() == null || ret.getData().getList() == null) {
            logger.error("invalid data when importing wishes from mihoyo");
            throw new ApiError(ErrorType.AUTHKEY_INVALID);
        }

        return ret.getData().getList();
    }

}

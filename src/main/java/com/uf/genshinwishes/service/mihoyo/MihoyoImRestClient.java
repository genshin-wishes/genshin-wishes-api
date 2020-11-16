package com.uf.genshinwishes.service.mihoyo;

import com.uf.genshinwishes.dto.mihoyo.MihoyoRetDTO;
import com.uf.genshinwishes.dto.mihoyo.MihoyoUserDTO;
import com.uf.genshinwishes.exception.ApiError;
import com.uf.genshinwishes.exception.ErrorType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
public class MihoyoImRestClient {
    @Value("${app.mihoyo.im-endpoint}")
    private String mihoyoImEndpoint;

    public MihoyoUserDTO getUserInfo(String authkey) throws ApiError {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(mihoyoImEndpoint + "/common/im/userClient/initUserChat")
            .queryParam("authkey", authkey)
            .queryParam("authkey_ver", 1)
            .queryParam("game_biz", "hk4e_global")
            .queryParam("sign_type", 2);

        MihoyoUserDTO user = new MihoyoUserDTO();

        MihoyoRetDTO body = getBody(builder);

        if (body.getRetcode() == -1 || body.getData() == null) {
            throw new ApiError(ErrorType.AUTHKEY_INVALID);
        }

        Map<String, String> infoMap = (Map<String, String>) body.getData();

        user.setMihoyoUid(infoMap.get("user_id"));
        user.setMihoyoUsername(infoMap.get("nickname"));

        return user;
    }

    private MihoyoRetDTO getBody(UriComponentsBuilder builder) throws ApiError {
        try {
            return new RestTemplate().postForEntity(builder.build(true).toUri(),
                "{\"device\":\"Mozilla\",\"language\":\"en\",\"system_info\":\"Mozilla/5.0\"}"
                , MihoyoRetDTO.class).getBody();
        } catch (Exception e) {
            throw new ApiError(ErrorType.MIHOYO_UNREACHABLE);
        }
    }
}

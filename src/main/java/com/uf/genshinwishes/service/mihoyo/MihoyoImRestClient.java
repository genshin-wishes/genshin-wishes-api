package com.uf.genshinwishes.service.mihoyo;

import com.uf.genshinwishes.dto.mihoyo.MihoyoInfoRetDTO;
import com.uf.genshinwishes.dto.mihoyo.MihoyoRetDTO;
import com.uf.genshinwishes.dto.mihoyo.MihoyoUserDTO;
import com.uf.genshinwishes.exception.ApiError;
import com.uf.genshinwishes.exception.ErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
public class MihoyoImRestClient {
    private final Logger logger = LoggerFactory.getLogger(MihoyoRestClient.class);

    @Autowired
    private RestTemplate restTemplate;

    private String mihoyoImEndpoint;

    MihoyoImRestClient(@Value("${app.mihoyo.im-endpoint}")
                           String mihoyoImEndpoint) {
        this.mihoyoImEndpoint = mihoyoImEndpoint;
    }

    public MihoyoUserDTO getUserInfo(String authkey) throws ApiError {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(mihoyoImEndpoint + "/common/im/userClient/initUserChat")
            .queryParam("authkey", authkey)
            .queryParam("authkey_ver", 1)
            .queryParam("game_biz", "hk4e_global")
            .queryParam("sign_type", 2);

        MihoyoInfoRetDTO body = getBody(builder);

        if (body.getRetcode() == -1 || body.getData() == null) {
            logger.error("ret -1 when importing wishes from mihoyo");
            throw new ApiError(ErrorType.AUTHKEY_INVALID);
        }

        return body.getData();
    }

    private MihoyoInfoRetDTO getBody(UriComponentsBuilder builder) throws ApiError {
        try {
            return restTemplate.postForEntity(builder.build(true).toUri(),
                "{\"device\":\"Mozilla\",\"language\":\"en\",\"system_info\":\"Mozilla/5.0\"}"
                , MihoyoInfoRetDTO.class).getBody();
        } catch (Exception e) {
            logger.error("Can't get user info from mihoyo", e);
            throw new ApiError(ErrorType.MIHOYO_UNREACHABLE);
        }
    }
}

package com.uf.genshinwishes.service.mihoyo;

import com.uf.genshinwishes.dto.mihoyo.MihoyoInfoRetDTO;
import com.uf.genshinwishes.dto.mihoyo.MihoyoUserDTO;
import com.uf.genshinwishes.exception.ApiError;
import com.uf.genshinwishes.exception.ErrorType;
import com.uf.genshinwishes.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

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

    public MihoyoUserDTO getUserInfo(Optional<User> user, String authkey) throws ApiError {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(mihoyoImEndpoint + "/common/im/userClient/initUserChat")
            .queryParam("authkey", authkey)
            .queryParam("authkey_ver", 1)
            .queryParam("game_biz", "hk4e_global")
            .queryParam("sign_type", 2);

        MihoyoInfoRetDTO body = getBody(user, builder);

        if (body.getRetcode() < 0 || body.getData() == null) {
            logger.error("ret -1 when importing wishes from mihoyo of #{} - {}", user.map(User::getId).map(String::valueOf).orElse("new user"), body.getMessage());
            throw new ApiError(ErrorType.AUTHKEY_INVALID);
        }

        return body.getData();
    }

    private MihoyoInfoRetDTO getBody(Optional<User> user, UriComponentsBuilder builder) throws ApiError {
        try {
            return restTemplate.postForEntity(builder.build(true).toUri(),
                "{\"device\":\"Mozilla\",\"language\":\"en\",\"system_info\":\"Mozilla/5.0\"}"
                , MihoyoInfoRetDTO.class).getBody();
        } catch (Exception e) {
            logger.error("Can't get #{} info from mihoyo", user.map(User::getId).map(String::valueOf).orElse("new user"), e);
            throw new ApiError(ErrorType.MIHOYO_UNREACHABLE);
        }
    }
}

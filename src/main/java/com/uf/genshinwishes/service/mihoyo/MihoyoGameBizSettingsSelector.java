package com.uf.genshinwishes.service.mihoyo;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * @author me 2021-05-31 10:58
 */
@Component
@ConfigurationProperties("app.mihoyo")
public class MihoyoGameBizSettingsSelector {

    @Setter
    private Map<String, Map<String, String>> regionSettings;


    public Optional<String> getImEndpoint(String gameBiz) {
        return Optional.of(regionSettings).map(p -> p.get(gameBiz)).map(p -> p.get("im-endpoint"));
    }

    public Optional<String> getWishEndpoint(String gameBiz) {
        return Optional.of(regionSettings).map(p -> p.get(gameBiz)).map(p -> p.get("wish-endpoint"));
    }


}

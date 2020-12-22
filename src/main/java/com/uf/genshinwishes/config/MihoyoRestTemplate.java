package com.uf.genshinwishes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class MihoyoRestTemplate {

    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

}

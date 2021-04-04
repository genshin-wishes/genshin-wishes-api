package com.uf.genshinwishes.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class MihoyoRestTemplate {
    @Value("${app.read-timeout}")
    private Integer readTimeout;
    @Value("${app.connect-timeout}")
    private Integer connectTimeout;

    @Bean
    public RestTemplate getRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        restTemplate.setRequestFactory(new SimpleClientHttpRequestFactory());
        SimpleClientHttpRequestFactory rf = (SimpleClientHttpRequestFactory) restTemplate
            .getRequestFactory();

        rf.setReadTimeout(readTimeout);
        rf.setConnectTimeout(connectTimeout);

        return restTemplate;
    }

    public Integer getReadTimeout() {
        return readTimeout;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }
}

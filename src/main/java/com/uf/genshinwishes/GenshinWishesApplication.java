package com.uf.genshinwishes;

import com.uf.genshinwishes.config.UserArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@SpringBootApplication()
public class GenshinWishesApplication {
    @Autowired
    private UserArgumentResolver userArgumentResolver;

    public static void main(String[] args) {
        SpringApplication.run(GenshinWishesApplication.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {

            @Override
            public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
                resolvers.add(userArgumentResolver);
            }
        };
    }


}



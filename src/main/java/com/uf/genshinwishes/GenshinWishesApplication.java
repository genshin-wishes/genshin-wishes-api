package com.uf.genshinwishes;

import com.uf.genshinwishes.config.SerializationSafeRepository;
import com.uf.genshinwishes.config.UserArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.SessionRepository;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.List;
import java.util.Locale;

@SpringBootApplication
@EnableRedisHttpSession
public class GenshinWishesApplication {
    @Autowired
    private UserArgumentResolver userArgumentResolver;
    @Autowired
    RedisTemplate<Object, Object> redisTemplate;

    public static void main(String[] args) {
        SpringApplication.run(GenshinWishesApplication.class, args);
    }

    @Primary
    @Bean
    public SessionRepository primarySessionRepository(RedisIndexedSessionRepository delegate) {
        return new SerializationSafeRepository(delegate, redisTemplate);
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

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver slr = new AcceptHeaderLocaleResolver();
        slr.setDefaultLocale(Locale.ENGLISH);
        return slr;
    }

    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasenames("i18n/export");  // name of the resource bundle
        source.setUseCodeAsDefaultMessage(true);
        return source;
    }
}



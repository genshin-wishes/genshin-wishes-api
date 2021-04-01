package com.uf.genshinwishes.security;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${app.client-url}")
    private String clientUrl;

    @NonNull
    private CustomOidcUserService customOidcUserService;
    @NonNull
    private CustomOauth2UserService customOauth2UserService;
    @NonNull
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .cors()
            .and()
            .csrf()
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .and()
            .authorizeRequests()
            .antMatchers("/oauth2/**", "/login/**", "/user", "/actuator/**").permitAll()
            .anyRequest().authenticated()
            .and()
            .oauth2Login()
            .userInfoEndpoint()
            .oidcUserService(customOidcUserService)
            .userService(customOauth2UserService)
            .and()
            .successHandler((httpServletRequest, httpServletResponse, authentication) -> {
                httpServletResponse.getWriter().write("");
            })
            .and()
            .rememberMe()
            .rememberMeServices(getRememberMeServices())
            .and()
            .exceptionHandling()
            .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), new AntPathRequestMatcher("/**"))
            .and()
            .logout(logout -> logout
                .permitAll()
                .logoutSuccessHandler((request, response, authentication) -> {
                        response.setStatus(HttpServletResponse.SC_OK);
                    }
                ));
    }

    @Bean
    public TokenBasedRememberMeServices getRememberMeServices() {
        TokenBasedRememberMeServices rememberMeServices = new TokenBasedRememberMeServices("rememberme", customUserDetailsService);
        rememberMeServices.setAlwaysRemember(true);
        return rememberMeServices;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(Arrays.asList(clientUrl));
        corsConfiguration.setAllowedMethods(
            Arrays.stream(RequestMethod.values())
                .map(RequestMethod::toString)
                .collect(Collectors.toList())
        );
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    private OAuth2AuthorizationRequest customAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest) {
        Map<String, Object> additionalParameters =new LinkedHashMap<>(authorizationRequest.getAdditionalParameters());
        additionalParameters.put("access_type", "offline");

        return OAuth2AuthorizationRequest.from(authorizationRequest)
            .additionalParameters(additionalParameters)
            .build();
    }
}


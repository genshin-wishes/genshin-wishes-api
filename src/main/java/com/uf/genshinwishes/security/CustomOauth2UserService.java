package com.uf.genshinwishes.security;

import com.uf.genshinwishes.model.User;
import com.uf.genshinwishes.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CustomOauth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        Map attributes = oAuth2User.getAttributes();

        Object email = attributes.get("email");
        Object openid = attributes.get("openid");

        if (email == null && openid == null)
            throw new OAuth2AuthenticationException(new OAuth2Error("permission-required"));

        String providerAndId = oAuth2UserRequest.getClientRegistration().getRegistrationId() + "_" + openid;

        User user = userService.retrieveOrInsertUser(email != null ? (String) email : providerAndId);

        return UserPrincipal.create(user);
    }
}

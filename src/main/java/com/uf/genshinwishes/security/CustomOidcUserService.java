package com.uf.genshinwishes.security;

import com.uf.genshinwishes.model.User;
import com.uf.genshinwishes.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CustomOidcUserService extends OidcUserService {

    @Autowired
    private UserService userService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        Map attributes = oidcUser.getAttributes();

        Object email = attributes.get("email");

        if (email == null) throw new OAuth2AuthenticationException(new OAuth2Error("email-permission-required"));

        User user = userService.retrieveOrInsertUser((String) email);

        return UserPrincipal.create(user);
    }
}

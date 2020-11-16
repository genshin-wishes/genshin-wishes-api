package com.uf.genshinwishes.config;

import com.uf.genshinwishes.model.User;
import com.uf.genshinwishes.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {
    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(User.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        if (!(SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof DefaultOAuth2User))
            return null;

        DefaultOAuth2User oidc = ((DefaultOAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        return userRepository.findByEmail((String) oidc.getAttributes().get("email"));
    }
}

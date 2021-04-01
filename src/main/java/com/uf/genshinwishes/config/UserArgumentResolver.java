package com.uf.genshinwishes.config;

import com.uf.genshinwishes.model.User;
import com.uf.genshinwishes.security.UserPrincipal;
import com.uf.genshinwishes.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {
    @Autowired
    private UserService userService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(User.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        if (SecurityContextHolder.getContext().getAuthentication() == null || !(SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof UserPrincipal))
            return null;

        UserPrincipal user = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        return userService.findByEmail(user.getUsername());
    }
}

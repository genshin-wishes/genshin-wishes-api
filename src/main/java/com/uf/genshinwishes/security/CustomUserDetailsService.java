package com.uf.genshinwishes.security;

import com.uf.genshinwishes.model.User;
import com.uf.genshinwishes.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        User user = userService.findByEmail(s);

        if (user == null) throw new UsernameNotFoundException("Username not found " + s);

        userService.updateLastLoggingDate(user);

        return UserPrincipal.create(user);
    }
}

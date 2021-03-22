package com.uf.genshinwishes.controller;

import com.uf.genshinwishes.model.User;
import com.uf.genshinwishes.dto.UserDTO;
import com.uf.genshinwishes.dto.mapper.UserMapper;
import com.uf.genshinwishes.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;

    @GetMapping("")
    public UserDTO isLoggedIn(User user) {
        return userMapper.toDto(user);
    }

    @GetMapping("/link")
    public UserDTO linkMihoyoAccount(User user, @RequestParam("authkey") String authkey) {
        userService.verifyUserIsUnlinkedAndLinkToMihoyo(user, authkey);

        return userMapper.toDto(user);
    }

    @PostMapping("/linkNew")
    public UserDTO linkNewMihoyoAccountAndDeleteOldWishes(User user, @RequestBody() String authkey) {
        userService.linkNewMihoyoAccountAndDeleteOldWishes(user, authkey);

        return userMapper.toDto(user);
    }


    @DeleteMapping("/delete")
    public void deleteUser(User user) {
        userService.deleteUser(user);
    }


    @PatchMapping("/lang")
    public void updateLang(User user, @RequestParam("lang") String lang) {
        userService.updateLang(user, lang);
    }
}

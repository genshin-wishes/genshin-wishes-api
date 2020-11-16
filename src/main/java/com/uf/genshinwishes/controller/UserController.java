package com.uf.genshinwishes.controller;

import com.uf.genshinwishes.model.User;
import com.uf.genshinwishes.service.UserService;
import com.uf.genshinwishes.service.WishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("")
    public User isLoggedIn(User user) {
        return user;
    }

    @GetMapping("/link")
    public User linkMihoyoAccount(User user, @RequestParam("authkey") String authkey) {
        userService.linkMihoyoUser(user, authkey);

        return user;
    }

    @PostMapping ("/linkNew")
    public User linkNewMihoyoAccountAndDeleteOldWishes(User user, @RequestBody() String authkey) {
        userService.linkNewMihoyoAccountAndDeleteOldWishes(user, authkey);

        return user;
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

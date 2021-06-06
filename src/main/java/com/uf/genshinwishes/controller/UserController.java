package com.uf.genshinwishes.controller;

import com.uf.genshinwishes.dto.UserDTO;
import com.uf.genshinwishes.dto.mapper.UserMapper;
import com.uf.genshinwishes.model.User;
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
    public UserDTO linkMihoyoAccount(User user, @RequestParam("authkey") String authkey, @RequestParam("game_biz") String gameBiz) {
        userService.verifyUserIsUnlinkedAndLinkToMihoyo(user, authkey, gameBiz);

        return userMapper.toDto(user);
    }

    @PostMapping("/linkNew")
    public UserDTO linkNewMihoyoAccountAndDeleteOldWishes(User user, @RequestBody() String authkey, @RequestParam String gameBiz) {
        userService.linkNewMihoyoAccountAndDeleteOldWishes(user, authkey, gameBiz);

        return userMapper.toDto(user);
    }

    @PatchMapping("/share")
    public String share(User user) {
        String profileId = user.getProfileId();

        if (profileId == null) {
            profileId = userService.initProfileId(user);
        }

        userService.share(user, true);

        return profileId;
    }

    @PatchMapping("/stopSharing")
    public void stopSharing(User user) {
        userService.share(user, false);
    }

    @DeleteMapping("/delete")
    public void deleteUser(User user) {
        userService.deleteUser(user);
    }

    @PatchMapping("/lang")
    public void updateLang(User user, @RequestParam("lang") String lang) {
        userService.updateLang(user, lang);
    }

    @PatchMapping("/wholeClock")
    public void updateWholeClock(User user, @RequestParam("wholeClock") Boolean wholeClock) {
        userService.updateWholeClock(user, wholeClock);
    }
}

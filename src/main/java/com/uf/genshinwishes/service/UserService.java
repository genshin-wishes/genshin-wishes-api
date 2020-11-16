package com.uf.genshinwishes.service;

import com.uf.genshinwishes.dto.mihoyo.MihoyoUserDTO;
import com.uf.genshinwishes.exception.ApiError;
import com.uf.genshinwishes.exception.ErrorType;
import com.uf.genshinwishes.model.User;
import com.uf.genshinwishes.repository.UserRepository;
import com.uf.genshinwishes.service.mihoyo.MihoyoImRestClient;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class UserService {
    private MihoyoImRestClient mihoyoImRestClient;
    private UserRepository userRepository;
    private WishService wishService;

    public User findByEmail(String email) {
        return this.userRepository.findByEmail(email);
    }

    public User insertUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setCreationDate(new Date());

        userRepository.save(user);

        return user;
    }

    public void linkMihoyoUser(User user, String authkey) throws ApiError {
        MihoyoUserDTO mihoyoUser = mihoyoImRestClient.getUserInfo(authkey);

        user.setMihoyoUid(mihoyoUser.getMihoyoUid());
        user.setMihoyoUsername(mihoyoUser.getMihoyoUsername());

        userRepository.save(user);
    }

    public void linkNewMihoyoAccountAndDeleteOldWishes(User user, String authkey) throws ApiError {
        this.linkMihoyoUser(user, authkey);

        wishService.deleteAll(user);
    }

    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    public void updateLang(User user, String lang) {
        if (!"fr".equals(lang) && !"en".equals(lang)) throw new ApiError(ErrorType.INVALID_LANG);

        user.setLang(lang);

        userRepository.save(user);
    }
}

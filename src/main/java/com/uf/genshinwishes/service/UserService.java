package com.uf.genshinwishes.service;

import com.uf.genshinwishes.dto.mihoyo.MihoyoUserDTO;
import com.uf.genshinwishes.exception.ApiError;
import com.uf.genshinwishes.exception.ErrorType;
import com.uf.genshinwishes.model.User;
import com.uf.genshinwishes.repository.UserRepository;
import com.uf.genshinwishes.service.mihoyo.MihoyoImRestClient;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Service;

import javax.persistence.LockModeType;
import javax.transaction.Transactional;
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
        user.setLastLoggingDate(new Date());

        userRepository.save(user);

        return user;
    }

    public void verifyUserIsUnlinkedAndLinkToMihoyo(User user, String authkey) throws ApiError {
        if(user.getMihoyoUid() != null) {
            throw new ApiError(ErrorType.MIHOYO_UID_DIFFERENT);
        }

        linkToMihoyo(user, authkey);
    }

    private void linkToMihoyo(User user, String authkey) {
        MihoyoUserDTO mihoyoUser = mihoyoImRestClient.getUserInfo(authkey);

        user.setMihoyoUid(mihoyoUser.getUser_id());
        user.setMihoyoUsername(mihoyoUser.getNickname());

        userRepository.save(user);
    }

    @Transactional
    @Lock(LockModeType.OPTIMISTIC)
    public void linkNewMihoyoAccountAndDeleteOldWishes(User user, String authkey) throws ApiError {
        this.linkToMihoyo(user, authkey);

        wishService.deleteAllUserWishes(user);
    }

    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    public void updateLang(User user, String lang) {
        if (!"fr".equals(lang) && !"en".equals(lang)) throw new ApiError(ErrorType.INVALID_LANG);

        user.setLang(lang);

        userRepository.save(user);
    }

    public void updateLastLoggingDate(User user) {
        user.setLastLoggingDate(new Date());

        userRepository.save(user);
    }
}

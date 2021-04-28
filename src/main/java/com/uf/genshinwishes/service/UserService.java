package com.uf.genshinwishes.service;

import com.uf.genshinwishes.dto.mihoyo.MihoyoUserDTO;
import com.uf.genshinwishes.exception.ApiError;
import com.uf.genshinwishes.exception.ErrorType;
import com.uf.genshinwishes.model.User;
import com.uf.genshinwishes.repository.UserRepository;
import com.uf.genshinwishes.service.mihoyo.MihoyoImRestClient;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

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
        user.setKey(UUID.randomUUID().toString());

        userRepository.save(user);

        return user;
    }

    public User retrieveOrInsertUser(String email) {
        User user = this.findByEmail(email);

        if (user == null) {
            user = this.insertUser(email);
        } else if (user.getKey() == null) {
            this.createKey(user);
        } else {
            this.updateLastLoggingDate(user);
        }

        return user;
    }

    public void createKey(User user) {
        user.setLastLoggingDate(new Date());
        user.setKey(UUID.randomUUID().toString());

        userRepository.save(user);
    }

    @Transactional
    public void verifyUserIsUnlinkedAndLinkToMihoyo(User user, String authkey) throws ApiError {
        if (user.getMihoyoUid() != null) {
            return; // already linked so we ignore
        }

        linkToMihoyo(user, authkey);
    }

    private void linkToMihoyo(User user, String authkey) {
        MihoyoUserDTO mihoyoUser = mihoyoImRestClient.getUserInfo(Optional.empty(), authkey);

        user.setMihoyoUid(mihoyoUser.getUser_id());
        user.setMihoyoUsername(mihoyoUser.getNickname());

        userRepository.save(user);
    }



    @Transactional
    public void linkNewMihoyoAccountAndDeleteOldWishes(User user, String authkey) throws ApiError {
        this.linkToMihoyo(user, authkey);

        wishService.deleteAllUserWishes(user);
    }

    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    public void updateLang(User user, String lang) {
        if (lang == null || "".equals(Locale.forLanguageTag(lang).toString())) throw new ApiError(ErrorType.INVALID_LANG);

        user.setLang(lang);

        userRepository.save(user);
    }

    public void updateWholeClock(User user, Boolean wholeClock) {
        user.setWholeClock(wholeClock);

        userRepository.save(user);
    }

    public void updateLastLoggingDate(User user) {
        user.setLastLoggingDate(new Date());

        userRepository.save(user);
    }

    public String initProfileId(User user) {
        String profileId = BigInteger.valueOf(Long.parseLong(Instant.now().toEpochMilli() + "" + user.getId())).toString(36);
        user.setProfileId(profileId);

        userRepository.save(user);

        return profileId;
    }

    public User findUserByProfileId(String profileId) {
        return userRepository.findByProfileId(profileId);
    }

    public void share(User user, boolean share) {
        user.setSharing(share);

        userRepository.save(user);
    }

    public Long getUsersCount() {
        return userRepository.countByMihoyoUsernameIsNotNull();
    }

    public static int getRegionOffset(User user) {
        if (user.getMihoyoUid() == null || user.getMihoyoUid().isEmpty()) return 0;

        switch (user.getMihoyoUid().charAt(0)) {
            case '6':
                return 5;
            case '7':
                return -1;
            default:
                logger.info("No region from user region {}, using Asia as default", user.getMihoyoUid());
            case '8':
                return -8;
        }
    }
}

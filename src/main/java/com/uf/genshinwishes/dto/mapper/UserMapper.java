package com.uf.genshinwishes.dto.mapper;

import com.uf.genshinwishes.dto.UserDTO;
import com.uf.genshinwishes.dto.WishDTO;
import com.uf.genshinwishes.dto.mihoyo.MihoyoWishLogDTO;
import com.uf.genshinwishes.model.Item;
import com.uf.genshinwishes.model.User;
import com.uf.genshinwishes.model.Wish;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Component
public class UserMapper {
    public UserDTO toDto(User user) {
        if(user == null) return null;

        UserDTO userDTO = new UserDTO();

        userDTO.setEmail(user.getEmail());
        userDTO.setLang(user.getLang());
        userDTO.setMihoyoUid(user.getMihoyoUid());
        userDTO.setMihoyoUsername(user.getMihoyoUsername());

        return userDTO;
    }
}

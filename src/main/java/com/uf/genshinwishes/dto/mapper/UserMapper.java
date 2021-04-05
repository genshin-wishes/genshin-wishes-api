package com.uf.genshinwishes.dto.mapper;

import com.uf.genshinwishes.dto.UserDTO;
import com.uf.genshinwishes.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDTO toDto(User user) {
        if(user == null) return null;

        UserDTO userDTO = new UserDTO();

        userDTO.setEmail(user.getEmail());
        userDTO.setLang(user.getLang());
        userDTO.setWholeClock(user.getWholeClock());
        userDTO.setMihoyoUid(user.getMihoyoUid());
        userDTO.setMihoyoUsername(user.getMihoyoUsername());
        userDTO.setProfileId(user.getProfileId());
        userDTO.setSharing(user.getSharing());

        return userDTO;
    }
}

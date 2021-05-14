package com.uf.genshinwishes.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String email;
    private String lang;
    private String region;
    private Boolean wholeClock;
    private String mihoyoUsername;
    private String mihoyoUid;
    private String profileId;
    private Boolean sharing;
}

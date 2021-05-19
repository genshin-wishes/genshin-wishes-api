package com.uf.genshinwishes.dto;

import com.uf.genshinwishes.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CountPerUserPerItemId {
    private User user;
    private Long itemId;
    private Long count;
}

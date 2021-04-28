package com.uf.genshinwishes.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WishDTO {
    private Long index;
    private Long pity;
    private Integer gachaType;
    private Long itemId;
    private LocalDateTime time;
}

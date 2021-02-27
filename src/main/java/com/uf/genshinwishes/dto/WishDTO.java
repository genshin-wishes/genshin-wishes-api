package com.uf.genshinwishes.dto;

import com.uf.genshinwishes.model.Item;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WishDTO {
    private Long id;
    private Long index;
    private String uid;
    private Integer gachaType;
    private Item item;
    private String itemName;
    private Date time;
}

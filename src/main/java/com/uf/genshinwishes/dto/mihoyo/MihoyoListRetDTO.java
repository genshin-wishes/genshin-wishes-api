package com.uf.genshinwishes.dto.mihoyo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MihoyoListRetDTO<T> {
    private MihoyoListDataDTO<T> data;
    private String message;
    private Integer retcode;
}

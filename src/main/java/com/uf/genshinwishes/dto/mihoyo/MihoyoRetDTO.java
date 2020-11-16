package com.uf.genshinwishes.dto.mihoyo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MihoyoRetDTO<T> {
    private T data;
    private String message;
    private Integer retcode;
}

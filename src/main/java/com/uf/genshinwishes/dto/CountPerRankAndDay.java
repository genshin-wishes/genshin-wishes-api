package com.uf.genshinwishes.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CountPerRankAndDay {
    private LocalDate date;
    private Integer rankType;
    private Long count;
}

package com.uf.genshinwishes.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ExclusivityDTO {
    private Long guarantee;
    private Long win;
    private Long loose;
    private Long delta;
}

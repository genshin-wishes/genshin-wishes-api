package com.uf.genshinwishes.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiError extends RuntimeException {

    private ErrorType errorType;
}

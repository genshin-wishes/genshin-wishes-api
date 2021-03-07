package com.uf.genshinwishes.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorType {
    AUTHKEY_INVALID(HttpStatus.INTERNAL_SERVER_ERROR),
    MIHOYO_UID_DIFFERENT(HttpStatus.FORBIDDEN),
    NO_MIHOYO_LINKED(HttpStatus.FORBIDDEN),
    MIHOYO_UNREACHABLE(HttpStatus.GATEWAY_TIMEOUT),
    INVALID_LANG(HttpStatus.BAD_REQUEST),
    NEW_WISHES_DURING_IMPORT(HttpStatus.BAD_REQUEST)

    //
    ;

    private HttpStatus status;
}

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
    NEW_WISHES_DURING_IMPORT(HttpStatus.BAD_REQUEST),
    NO_SUITABLE_ENDPOINT_FOR_GAME_BIZ(HttpStatus.BAD_REQUEST),
    NO_REGION_FROM_USER_UID(HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_FILTERS(HttpStatus.BAD_REQUEST),
    ALREADY_IMPORTING(HttpStatus.BAD_REQUEST),
    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND),
    MISSING_ITEM(HttpStatus.INTERNAL_SERVER_ERROR),
    ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
    IMPORT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR)

    //
    ;

    private HttpStatus status;
}

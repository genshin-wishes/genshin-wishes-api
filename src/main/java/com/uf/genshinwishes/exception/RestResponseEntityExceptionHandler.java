package com.uf.genshinwishes.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestResponseEntityExceptionHandler
    extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value
        = {ApiError.class})
    protected ResponseEntity<Object> handleConflict(
        RuntimeException ex, WebRequest request) {
        return handleExceptionInternal(ex, ((ApiError) ex).getErrorType(),
            new HttpHeaders(), ((ApiError) ex).getErrorType().getStatus(), request);
    }
}

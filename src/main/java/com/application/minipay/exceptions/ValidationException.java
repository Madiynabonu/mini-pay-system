package com.application.minipay.exceptions;

import org.springframework.http.HttpStatus;

public class ValidationException extends BusinessException {


    public ValidationException(String message) {
        super("VALIDATION_ERROR", HttpStatus.BAD_REQUEST,message);
    }
}
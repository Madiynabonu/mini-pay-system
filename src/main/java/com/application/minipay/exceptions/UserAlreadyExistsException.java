package com.application.minipay.exceptions;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends BusinessException {


    public UserAlreadyExistsException(String phone) {
        super("USER_ALREADY_EXISTS", HttpStatus.CONFLICT, "User already exists with phone: " + phone);
    }
}



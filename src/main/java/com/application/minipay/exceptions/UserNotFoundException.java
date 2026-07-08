package com.application.minipay.exceptions;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class UserNotFoundException extends BusinessException {


    public UserNotFoundException(UUID userId) {
        super("USER_NOT_FOUND", HttpStatus.NOT_FOUND, "User not found: " + userId);
    }
}



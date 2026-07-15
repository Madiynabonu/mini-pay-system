package com.application.minipay.exceptions;

import org.springframework.http.HttpStatus;

public class ProviderAlreadyExistsException extends BusinessException {


    public ProviderAlreadyExistsException(String code) {
        super("PROVIDER_ALREADY_EXISTS", HttpStatus.CONFLICT, "Provider already exists: " + code);
    }
}



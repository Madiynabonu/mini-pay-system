package com.application.minipay.exceptions;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class ProviderNotAvailableException extends BusinessException {

    public ProviderNotAvailableException(UUID providerId) {
        super("PROVIDER_NOT_AVAILABLE", HttpStatus.BAD_REQUEST, "Provider not available: " + providerId);
    }
}



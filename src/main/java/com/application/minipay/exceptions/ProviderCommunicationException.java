package com.application.minipay.exceptions;

public class ProviderCommunicationException extends RuntimeException {

    public ProviderCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.application.minipay.exceptions;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class TransactionNotFoundException extends BusinessException {


    public TransactionNotFoundException(UUID transactionId) {
        super("TRANSACTION_NOT_FOUND", HttpStatus.NOT_FOUND, "Transaction not found: " + transactionId);
    }
}
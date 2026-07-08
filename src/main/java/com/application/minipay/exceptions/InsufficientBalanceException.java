package com.application.minipay.exceptions;

import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.UUID;

public class InsufficientBalanceException extends BusinessException {

    public InsufficientBalanceException(UUID userId, BigDecimal balance) {
        super("INSUFFICIENT_BALANCE", HttpStatus.UNPROCESSABLE_ENTITY, "Insufficient balance: " + userId + " balance: " + balance);
    }
}



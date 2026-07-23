package com.application.minipay.services;

import com.application.minipay.dtos.TransactionResponseDTO;

public record TransactionChargeResult(TransactionResponseDTO transaction , boolean isNewTransaction) {

}

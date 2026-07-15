package com.application.minipay.dtos;

import com.application.minipay.enums.Currency;
import com.application.minipay.enums.Status;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;


@Getter
@Setter
@Builder
public class TransactionResponseDTO {

    private UUID transactionId;

    private UUID userId;

    private UUID providerId;

    private String serviceAccount;

    private BigDecimal amount;

    private Currency currency;

    private Status status;

    private String failureReason;

    private Instant createdAt;
}

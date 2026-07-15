package com.application.minipay.dtos;

import com.application.minipay.enums.Currency;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.UUID;


@Getter
@Setter
@ToString
@Builder
public class TransactionRequestDTO {


    private UUID userId;

    private UUID providerId;


    private String serviceAccount;

    private BigDecimal amount;

    private Currency currency;


}

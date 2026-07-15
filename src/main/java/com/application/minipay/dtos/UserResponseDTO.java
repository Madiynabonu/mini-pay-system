package com.application.minipay.dtos;

import com.application.minipay.enums.Currency;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;


@Getter
@Setter
@Builder
public class UserResponseDTO {


    private UUID id;

    private String fullName;

    private String phone;

    private BigDecimal balance;

    private Currency currency;

    private Instant createdAt;

    private Instant updatedAt;


}

package com.application.minipay.dtos;

import com.application.minipay.enums.Currency;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
@Builder
public class UserRequestDTO {


    private String fullName;

    private String phone;

    private BigDecimal balance;

    private Currency currency;


}

package com.application.minipay.provider;

import com.application.minipay.enums.Currency;

import java.math.BigDecimal;

public record ProviderChargeRequest(
        String serviceAccount,
        BigDecimal amount,
        Currency currency
) {

}

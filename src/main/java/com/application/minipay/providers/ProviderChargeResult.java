package com.application.minipay.providers;

import com.application.minipay.enums.Status;

public record ProviderChargeResult(
        Status status,
        String providerRef,
        String failureReason
) {
}

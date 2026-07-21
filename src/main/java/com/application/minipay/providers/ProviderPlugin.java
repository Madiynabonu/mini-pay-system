package com.application.minipay.providers;

public interface ProviderPlugin {
    ProviderChargeResult charge(ProviderChargeRequest request);
}

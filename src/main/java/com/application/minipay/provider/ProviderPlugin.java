package com.application.minipay.provider;

public interface ProviderPlugin {
    ProviderChargeResult charge(ProviderChargeRequest request);
}

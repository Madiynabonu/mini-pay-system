package com.application.minipay.providers;

import com.application.minipay.enums.Status;

import java.util.UUID;

public class ProviderDummyAdapter implements ProviderPlugin {
    @Override
    public ProviderChargeResult charge(ProviderChargeRequest request) {


        return new ProviderChargeResult(Status.SUCCESS, UUID.randomUUID().toString(), null);
    }
}



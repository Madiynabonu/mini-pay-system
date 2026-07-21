package com.application.minipay.providers;

import com.application.minipay.enums.Status;
import com.application.minipay.exceptions.ProviderCommunicationException;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
@Component("PROVIDER_B")
public class ProviderBAdapter implements ProviderPlugin {
    @Override
    public ProviderChargeResult charge(ProviderChargeRequest request) {

        try {

            Thread.sleep(ThreadLocalRandom.current().nextInt(300, 800));
            double random = Math.random();

            if (random < 0.1) {
                return new ProviderChargeResult(Status.FAILED, null, "PROVIDER_DECLINED");

            }

            return new ProviderChargeResult(Status.SUCCESS, UUID.randomUUID().toString(), null);

        } catch (InterruptedException e) {
            throw new ProviderCommunicationException("Provider B bilan bog'lanishda xatolik", e);
        }

    }
}
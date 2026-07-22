package com.application.minipay.services;

import com.application.minipay.providers.ProviderPlugin;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component

public class ProviderResolver {

    private final Map<String, ProviderPlugin> providers;

    public ProviderResolver(Map<String, ProviderPlugin> providers) {
        this.providers = providers;
    }

    public ProviderPlugin resolve(String code) {

        ProviderPlugin plugin = providers.get(code);

        if (plugin == null) {
            throw new IllegalStateException("No adapter registered for provider code: " + code);
        }

        return plugin;


    }
}

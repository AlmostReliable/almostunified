package com.almostreliable.unified;

import java.util.ServiceLoader;

@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
public final class PlatformLoader {
    static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        AlmostUnified.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}

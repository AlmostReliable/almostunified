package com.almostreliable.unitagged.api;

import com.almostreliable.unitagged.UniTaggedCommon;

import java.util.ServiceLoader;

public class PlatformLoader {
    static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        UniTaggedCommon.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}

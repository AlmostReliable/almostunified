package com.almostreliable.unified;

import com.almostreliable.unified.api.constant.ModConstants;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class AlmostUnifiedPlatformFabric implements AlmostUnifiedPlatform {

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    @Override
    public Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(ModConstants.ALMOST_UNIFIED);
    }

    @Override
    public Path getDebugLogPath() {
        return FabricLoader
            .getInstance()
            .getGameDir()
            .resolve("logs")
            .resolve(ModConstants.ALMOST_UNIFIED)
            .resolve("debug");
    }
}

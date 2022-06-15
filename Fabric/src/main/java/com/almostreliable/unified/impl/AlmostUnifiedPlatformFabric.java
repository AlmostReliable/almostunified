package com.almostreliable.unified.impl;

import com.almostreliable.unified.AlmostUnifiedPlatform;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class AlmostUnifiedPlatformFabric implements AlmostUnifiedPlatform {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir();
    }
}

package com.almostreliable.unitagged.impl;

import com.almostreliable.unitagged.api.UniTaggedPlatform;
import net.fabricmc.loader.api.FabricLoader;

public class UniTaggedPlatformFabric implements UniTaggedPlatform {

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
}

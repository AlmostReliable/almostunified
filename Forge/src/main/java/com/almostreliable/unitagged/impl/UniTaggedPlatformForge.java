package com.almostreliable.unitagged.impl;

import com.almostreliable.unitagged.api.UniTaggedPlatform;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;

public class UniTaggedPlatformForge implements UniTaggedPlatform {

    @Override
    public String getPlatformName() {
        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }
}

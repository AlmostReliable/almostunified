package com.almostreliable.unified;

import com.almostreliable.unified.api.constant.ModConstants;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.moddiscovery.ModInfo;

import java.nio.file.Path;

public class AlmostUnifiedPlatformNeoForge implements AlmostUnifiedPlatform {

    @Override
    public boolean isModLoaded(String modId) {
        if (ModList.get() == null) {
            return FMLLoader.getCurrent().getLoadingModList().getMods().stream().map(ModInfo::getModId).anyMatch(modId::equals);
        }
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isClient() {
        return FMLLoader.getCurrent().getDist() == Dist.CLIENT;
    }

    @Override
    public Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get().resolve(ModConstants.ALMOST_UNIFIED);
    }

    @Override
    public Path getDebugLogPath() {
        return FMLPaths.GAMEDIR.get().resolve("logs").resolve(ModConstants.ALMOST_UNIFIED).resolve("debug");
    }
}

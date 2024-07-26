package com.almostreliable.unified;

import com.google.auto.service.AutoService;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

@AutoService(AlmostUnifiedPlatform.class)
public class AlmostUnifiedPlatformFabric implements AlmostUnifiedPlatform {

    @Override
    public Platform getPlatform() {
        return Platform.FABRIC;
    }

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
        return FabricLoader.getInstance().getConfigDir().resolve(BuildConfig.MOD_ID);
    }

    @Override
    public Path getDebugLogPath() {
        return FabricLoader.getInstance().getGameDir().resolve("logs").resolve(BuildConfig.MOD_ID).resolve("debug");
    }

    @Override
    public Set<TagKey<Item>> getStoneStrataTags(Collection<String> stoneStrataIds) {
        return Set.of();
    }
}

package com.almostreliable.unified;

import com.google.auto.service.AutoService;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModInfo;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@AutoService(AlmostUnifiedPlatform.class)
public class AlmostUnifiedPlatformNeoForge implements AlmostUnifiedPlatform {

    @Override
    public Platform getPlatform() {
        return Platform.NEO_FORGE;
    }

    @Override
    public boolean isModLoaded(String modId) {
        if (ModList.get() == null) {
            return LoadingModList.get().getMods().stream().map(ModInfo::getModId).anyMatch(modId::equals);
        }
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isClient() {
        return FMLLoader.getDist() == Dist.CLIENT;
    }

    @Override
    public Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get().resolve(BuildConfig.MOD_ID);
    }

    @Override
    public Path getDebugLogPath() {
        return FMLPaths.GAMEDIR.get().resolve("logs").resolve(BuildConfig.MOD_ID).resolve("debug");
    }

    @Override
    public Set<TagKey<Item>> getStoneStrataTags(Collection<String> stoneStrataIds) {
        return stoneStrataIds
                .stream()
                .map(id -> ResourceLocation.fromNamespaceAndPath("forge", "ores_in_ground/" + id))
                .map(id -> TagKey.create(Registries.ITEM, id))
                .collect(Collectors.toSet());
    }
}

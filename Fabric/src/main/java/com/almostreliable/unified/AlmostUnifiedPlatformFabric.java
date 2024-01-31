package com.almostreliable.unified;

import com.almostreliable.unified.api.ModConstants;
import com.almostreliable.unified.compat.AdAstraRecipeUnifier;
import com.almostreliable.unified.compat.AmethystImbuementRecipeUnifier;
import com.almostreliable.unified.compat.GregTechModernRecipeUnifier;
import com.almostreliable.unified.compat.ModernIndustrializationRecipeUnifier;
import com.almostreliable.unified.api.UnifierRegistry;
import com.google.auto.service.AutoService;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.nio.file.Path;
import java.util.List;
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
    public Path getLogPath() {
        return FabricLoader.getInstance().getGameDir().resolve("logs").resolve(BuildConfig.MOD_ID);
    }

    @Override
    public void bindRecipeHandlers(UnifierRegistry factory) {
        factory.registerForMod(ModConstants.AD_ASTRA, new AdAstraRecipeUnifier());
        factory.registerForMod(ModConstants.AMETHYST_IMBUEMENT, new AmethystImbuementRecipeUnifier());
        factory.registerForMod(ModConstants.GREGTECH_MODERN, new GregTechModernRecipeUnifier());
        factory.registerForMod(ModConstants.MODERN_INDUSTRIALIZATION, new ModernIndustrializationRecipeUnifier());
    }

    @Override
    public Set<TagKey<Item>> getStoneStrataTags(List<String> stoneStrataIds) {
        return Set.of();
    }
}

package com.almostreliable.unified;

import com.almostreliable.unified.api.ModConstants;
import com.almostreliable.unified.compat.AmethystImbuementRecipeUnifier;
import com.almostreliable.unified.recipe.unifier.RecipeHandlerFactory;
import com.almostreliable.unified.utils.UnifyTag;
import com.google.auto.service.AutoService;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
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
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
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
    public void bindRecipeHandlers(RecipeHandlerFactory factory) {
        factory.registerForMod(ModConstants.AMETHYST_IMBUEMENT, new AmethystImbuementRecipeUnifier());
    }

    @Override
    public Set<UnifyTag<Item>> getStoneStrataTags(List<String> stoneStrataIds) {
        return Set.of();
    }
}

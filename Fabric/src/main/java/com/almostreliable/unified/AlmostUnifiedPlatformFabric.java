package com.almostreliable.unified;

import com.almostreliable.unified.recipe.unifier.RecipeHandlerFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

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

    }
}

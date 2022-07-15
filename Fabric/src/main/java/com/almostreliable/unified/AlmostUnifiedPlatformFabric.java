package com.almostreliable.unified;

import com.almostreliable.unified.recipe.handler.RecipeHandlerFactory;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class AlmostUnifiedPlatformFabric implements AlmostUnifiedPlatform {

    @Override
    public Platform getPlatform() {
        return Platform.Fabric;
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

    @Override
    public Path getLogPath() {
        return FabricLoader.getInstance().getGameDir().resolve("logs").resolve(BuildConfig.MOD_ID);
    }

    @Override
    public void bindRecipeHandlers(RecipeHandlerFactory factory) {

    }

    @Override
    public AlmostUnifiedRuntime createRuntime(RecipeHandlerFactory factory) {
        return new AlmostUnifiedRuntimeFabric(factory);
    }
}

package com.almostreliable.unified;

import com.almostreliable.unified.recipe.handler.RecipeHandlerFactory;
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

    @Override
    public void bindRecipeHandlers(RecipeHandlerFactory factory) {

    }

    @Override
    public AlmostUnifiedRuntime createRuntime(RecipeHandlerFactory factory) {
        return new AlmostUnifiedRuntimeFabric(factory);
    }
}

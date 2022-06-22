package com.almostreliable.unified;

import com.almostreliable.unified.api.ModConstants;
import com.almostreliable.unified.compat.ie.IERecipeHandler;
import com.almostreliable.unified.recipe.handler.RecipeHandlerFactory;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class AlmostUnifiedPlatformForge implements AlmostUnifiedPlatform {

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

    @Override
    public Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public void bindRecipeHandlers(RecipeHandlerFactory factory) {
        factory.registerForMod(ModConstants.IE, new IERecipeHandler());
    }

    @Override
    public AlmostUnifiedRuntime createRuntime(RecipeHandlerFactory factory) {
        return new AlmostUnifiedRuntimeForge(factory);
    }
}

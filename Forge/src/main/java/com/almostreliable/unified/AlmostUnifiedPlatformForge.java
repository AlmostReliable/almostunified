package com.almostreliable.unified;

import com.almostreliable.unified.api.ModConstants;
import com.almostreliable.unified.compat.IERecipeUnifier;
import com.almostreliable.unified.recipe.unifier.RecipeHandlerFactory;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class AlmostUnifiedPlatformForge implements AlmostUnifiedPlatform {

    @Override
    public Platform getPlatform() {
        return Platform.Forge;
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
        return FMLPaths.CONFIGDIR.get().resolve(BuildConfig.MOD_ID);
    }

    @Override
    public Path getLogPath() {
        return FMLPaths.GAMEDIR.get().resolve("logs").resolve(BuildConfig.MOD_ID);
    }

    @Override
    public void bindRecipeHandlers(RecipeHandlerFactory factory) {
        factory.registerForMod(ModConstants.IE, new IERecipeUnifier());
    }
}

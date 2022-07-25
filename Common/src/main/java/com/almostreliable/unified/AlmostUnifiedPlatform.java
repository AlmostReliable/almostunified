package com.almostreliable.unified;

import com.almostreliable.unified.recipe.unifier.RecipeHandlerFactory;

import java.nio.file.Path;

public interface AlmostUnifiedPlatform {

    AlmostUnifiedPlatform INSTANCE = PlatformLoader.load(AlmostUnifiedPlatform.class);

    /**
     * Gets the current platform
     *
     * @return The current platform.
     */
    Platform getPlatform();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    Path getConfigPath();

    Path getLogPath();

    void bindRecipeHandlers(RecipeHandlerFactory factory);

    AlmostUnifiedRuntime createRuntime(RecipeHandlerFactory factory);
}

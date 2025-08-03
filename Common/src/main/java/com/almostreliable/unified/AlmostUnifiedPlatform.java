package com.almostreliable.unified;

import net.minecraft.world.item.crafting.RecipeManager;

import com.almostreliable.unified.unification.recipe.RecipeLinkFactory;

import java.nio.file.Path;
import java.util.ServiceLoader;

public interface AlmostUnifiedPlatform {

    AlmostUnifiedPlatform INSTANCE = ServiceLoader.load(AlmostUnifiedPlatform.class)
        .findFirst()
        .orElseThrow(() -> new NullPointerException("Failed to load platform service."));

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

    boolean isClient();

    Path getConfigPath();

    Path getDebugLogPath();

    RecipeLinkFactory getRecipeLinkFactory(RecipeManager recipeManager, boolean cache);

    enum Platform {
        NEO_FORGE,
        FABRIC
    }
}

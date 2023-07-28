package com.almostreliable.unified;

import com.almostreliable.unified.recipe.unifier.RecipeHandlerFactory;
import com.almostreliable.unified.utils.UnifyTag;
import net.minecraft.world.item.Item;

import java.nio.file.Path;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

public interface AlmostUnifiedPlatform {

    AlmostUnifiedPlatform INSTANCE = load(AlmostUnifiedPlatform.class);

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

    Path getLogPath();

    void bindRecipeHandlers(RecipeHandlerFactory factory);

    Set<UnifyTag<Item>> getStoneStrataTags(List<String> stoneStrataIds);

    static <T> T load(Class<T> clazz) {
        T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        AlmostUnified.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }

    enum Platform {
        FORGE,
        FABRIC
    }
}

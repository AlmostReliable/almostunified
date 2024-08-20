package com.almostreliable.unified.api.plugin;

import com.almostreliable.unified.api.unification.recipe.RecipeUnifier;
import com.almostreliable.unified.api.unification.recipe.RecipeUnifierRegistry;
import net.minecraft.resources.ResourceLocation;

/**
 * Implemented by plugins that wish to register their own {@link RecipeUnifier}s.
 * <p>
 * NeoForge plugins should attach the {@link AlmostUnifiedNeoPlugin} annotation for discovery.<br>
 * Fabric plugins should use the {@code almostunified} entrypoint.
 */
public interface AlmostUnifiedPlugin {

    /**
     * Returns the identifier of the plugin.
     * <p>
     * If your mod has multiple plugins for different modules, make
     * sure they are unique.
     * <p>
     * If you register a recipe unifier although Almost Unified already
     * ships a recipe unifier for your recipes, yours will take precedence.
     *
     * @return the plugin id
     */
    ResourceLocation getPluginId();

    /**
     * Allows registration of custom {@link RecipeUnifier}s.
     *
     * @param registry the {@link RecipeUnifierRegistry} to register with
     */
    default void registerRecipeUnifiers(RecipeUnifierRegistry registry) {}
}

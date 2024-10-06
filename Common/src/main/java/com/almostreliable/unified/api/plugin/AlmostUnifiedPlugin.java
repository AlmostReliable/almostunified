package com.almostreliable.unified.api.plugin;

import net.minecraft.resources.ResourceLocation;

import com.almostreliable.unified.api.unification.recipe.CustomIngredientUnifier;
import com.almostreliable.unified.api.unification.recipe.CustomIngredientUnifierRegistry;
import com.almostreliable.unified.api.unification.recipe.RecipeUnifier;
import com.almostreliable.unified.api.unification.recipe.RecipeUnifierRegistry;

/**
 * Implemented by plugins that wish to register their own unifiers.
 * <p>
 * NeoForge plugins should attach the {@link AlmostUnifiedNeoPlugin} annotation for discovery.<br>
 * Fabric plugins should use the {@code almostunified} entrypoint.
 *
 * @since 1.0.0
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

    /**
     * Allows registration of custom {@link CustomIngredientUnifier}s.
     *
     * @param registry the {@link CustomIngredientUnifierRegistry} to register with
     * @since 1.2.0
     */
    default void registerCustomIngredientUnifiers(CustomIngredientUnifierRegistry registry) {}
}

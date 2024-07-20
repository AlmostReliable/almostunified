package com.almostreliable.unified.api;

import com.almostreliable.unified.api.recipe.RecipeData;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.recipe.unifier.GenericRecipeUnifier;
import net.minecraft.resources.ResourceLocation;

/**
 * The registry holding all {@link RecipeUnifier}s.
 * <p>
 * Unifiers can be registered per specific recipe types or per specific mod id.
 */
public interface UnifierRegistry {

    /**
     * Registers a {@link RecipeUnifier} for a specific recipe type.
     * <p>
     * Recipe type based unifiers override mod id based unifiers.<br>
     * Registering a custom unifier will always disable the {@link GenericRecipeUnifier}.
     *
     * @param recipeType the recipe type to register the unifier for
     * @param unifier    the unifier
     */
    void registerForRecipeType(ResourceLocation recipeType, RecipeUnifier unifier);

    /**
     * Registers a {@link RecipeUnifier} for a specific mod id.
     * <p>
     * Mod id based unifiers will only apply if no recipe type based unifiers are registered
     * for the respective recipe.<br>
     * Registering a custom unifier will always disable the {@link GenericRecipeUnifier}.
     *
     * @param modId   the mod id to register the unifier for
     * @param unifier the unifier
     */
    void registerForModId(String modId, RecipeUnifier unifier);

    /**
     * Retrieves the respective {@link RecipeUnifier} for the given {@link RecipeData}.
     *
     * @param recipeData the recipe data
     * @return the unifier for the given recipe data
     */
    RecipeUnifier getUnifier(RecipeData recipeData);
}

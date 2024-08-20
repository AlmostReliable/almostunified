package com.almostreliable.unified.api;

import com.almostreliable.unified.api.recipe.GenericRecipeUnifier;
import com.almostreliable.unified.api.recipe.RecipeData;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import net.minecraft.resources.ResourceLocation;

/**
 * The registry holding all {@link RecipeUnifier}s.
 * <p>
 * {@link RecipeUnifier}s can be registered per recipe type or per mod id.
 */
public interface RecipeUnifierRegistry {

    /**
     * Registers a {@link RecipeUnifier} for a specific recipe type.
     * <p>
     * Recipe-type-based recipe unifiers override mod-id-based recipe unifiers.<br>
     * Registering a custom recipe unifier will always disable the bundled recipe unifiers
     * like the {@link GenericRecipeUnifier}.
     *
     * @param recipeType    the recipe type to register the recipe unifier for
     * @param recipeUnifier the recipe unifier
     */
    void registerForRecipeType(ResourceLocation recipeType, RecipeUnifier recipeUnifier);

    /**
     * Registers a {@link RecipeUnifier} for a specific mod id.
     * <p>
     * Mod-id-based recipe unifiers will only apply if no recipe-type-based recipe unifiers
     * are registered for the respective recipe.<br>
     * Registering a custom recipe unifier will always disable the bundled recipe unifiers
     * like the {@link GenericRecipeUnifier}.
     *
     * @param modId         the mod id to register the recipe unifier for
     * @param recipeUnifier the recipe unifier
     */
    void registerForModId(String modId, RecipeUnifier recipeUnifier);

    /**
     * Retrieves the respective {@link RecipeUnifier} for the given {@link RecipeData}.
     *
     * @param recipeData the recipe data
     * @return the recipe unifier for the given recipe data
     */
    RecipeUnifier getRecipeUnifier(RecipeData recipeData);
}

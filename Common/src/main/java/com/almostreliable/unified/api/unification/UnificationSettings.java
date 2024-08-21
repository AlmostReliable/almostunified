package com.almostreliable.unified.api.unification;

import com.almostreliable.unified.api.unification.recipe.RecipeData;
import net.minecraft.resources.ResourceLocation;

/**
 * Interface exposing unification information for a single unification config.
 * <p>
 * There exists one instance for each config.<br>
 * Because {@link UnificationLookup}s are not composable, this interface should only be used when specific settings
 * need to be checked.
 *
 * @since 1.0.0
 */
public interface UnificationSettings extends UnificationLookup {

    /**
     * Returns the name of the unification config these settings are for.
     * <p>
     * The name of the config is the file name of the JSON file within the unification subfolder without the file
     * extension.
     *
     * @return the name of the config
     */
    String getName();

    /**
     * Returns the instance of the {@link ModPriorities} these settings are based on.
     *
     * @return the {@link ModPriorities}
     */
    ModPriorities getModPriorities();

    /**
     * Returns whether the given {@link RecipeData} should be included in the unification process.
     * <p>
     * This method is a quick way to check the recipe id and type.
     *
     * @param recipe the recipe to check
     * @return true if the recipe should be included, false otherwise
     */
    default boolean shouldIncludeRecipe(RecipeData recipe) {
        return shouldIncludeRecipeType(recipe) && shouldIncludeRecipeId(recipe);
    }

    /**
     * Returns whether the given recipe type should be included in the unification process.
     *
     * @param type the recipe type to check
     * @return true if the recipe type should be included, false otherwise
     */
    boolean shouldIncludeRecipeType(ResourceLocation type);

    /**
     * Returns whether the recipe type of the given {@link RecipeData} should be included in the unification process.
     *
     * @param recipe the recipe to check
     * @return true if the recipe type should be included, false otherwise
     */
    default boolean shouldIncludeRecipeType(RecipeData recipe) {
        return shouldIncludeRecipeType(recipe.getType());
    }

    /**
     * Returns whether the given recipe id should be included in the unification process.
     *
     * @param id the recipe id to check
     * @return true if the recipe id should be included, false otherwise
     */
    boolean shouldIncludeRecipeId(ResourceLocation id);

    /**
     * Returns whether the recipe id of the given {@link RecipeData} should be included in the unification process.
     *
     * @param recipe the recipe to check
     * @return true if the recipe id should be included, false otherwise
     */
    default boolean shouldIncludeRecipeId(RecipeData recipe) {
        return shouldIncludeRecipeId(recipe.getId());
    }

    /**
     * Returns whether variant items of this config should be hidden in recipe viewers.
     *
     * @return true if variant items should be hidden, false otherwise
     */
    boolean shouldHideVariantItems();

    /**
     * Returns whether loot tables should be unified with the unification of this config.
     *
     * @return true if loot tables should be unified, false otherwise
     */
    boolean shouldUnifyLoot();

    /**
     * Returns whether the given loot table should be included in the unification process.
     *
     * @param table the loot table to check
     * @return true if the loot table should be included, false otherwise
     */
    boolean shouldIncludeLootTable(ResourceLocation table);
}

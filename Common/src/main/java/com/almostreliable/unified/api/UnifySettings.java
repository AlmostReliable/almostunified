package com.almostreliable.unified.api;

import com.almostreliable.unified.api.recipe.RecipeData;
import net.minecraft.resources.ResourceLocation;

public interface UnifySettings {

    ModPriorities getModPriorities();

    default boolean shouldIncludeRecipe(RecipeData recipe) {
        return shouldIncludeRecipeId(recipe.getId()) && shouldIncludeRecipeType(recipe.getType());
    }

    boolean shouldIncludeRecipeId(ResourceLocation id);

    boolean shouldIncludeRecipeType(ResourceLocation type);

    boolean shouldUnifyLootTable(ResourceLocation table);

    void clearCache();

    boolean hideNonPreferredItemsInRecipeViewers();

    String getName();
}

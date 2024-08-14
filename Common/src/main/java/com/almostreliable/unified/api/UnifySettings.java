package com.almostreliable.unified.api;

import net.minecraft.resources.ResourceLocation;
import com.almostreliable.unified.api.recipe.RecipeData;

public interface UnifySettings {

    ModPriorities getModPriorities();

    default boolean shouldIncludeRecipe(RecipeData recipe) {
        return shouldIncludeRecipeId(recipe.getId()) && shouldIncludeRecipeType(recipe.getType());
    }

    boolean shouldIncludeRecipeId(ResourceLocation id);

    boolean shouldIncludeRecipeType(ResourceLocation type);

    boolean shouldUnifyLootTable(ResourceLocation table);

    boolean enableLootUnification();

    void clearCache();

    boolean shouldHideVariantItems();

    String getName();
}

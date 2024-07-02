package com.almostreliable.unified.api;

import com.almostreliable.unified.api.recipe.RecipeData;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import net.minecraft.resources.ResourceLocation;

public interface UnifierRegistry {

    void registerForRecipeType(ResourceLocation recipeType, RecipeUnifier unifier);

    void registerForModId(String modId, RecipeUnifier unifier);

    RecipeUnifier getUnifier(RecipeData recipeData);
}

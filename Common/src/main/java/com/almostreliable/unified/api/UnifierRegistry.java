package com.almostreliable.unified.api;

import com.almostreliable.unified.api.recipe.RecipeData;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import net.minecraft.resources.ResourceLocation;

public interface UnifierRegistry {
    RecipeUnifier getUnifier(RecipeData recipeData);

    void registerForType(ResourceLocation type, RecipeUnifier transformer);

    void registerForMod(String mod, RecipeUnifier transformer);
}

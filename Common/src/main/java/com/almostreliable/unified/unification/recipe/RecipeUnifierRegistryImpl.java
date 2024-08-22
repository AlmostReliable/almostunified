package com.almostreliable.unified.unification.recipe;

import com.almostreliable.unified.api.unification.bundled.GenericRecipeUnifier;
import com.almostreliable.unified.api.unification.bundled.ShapedRecipeUnifier;
import com.almostreliable.unified.api.unification.bundled.SmithingRecipeUnifier;
import com.almostreliable.unified.api.unification.recipe.RecipeData;
import com.almostreliable.unified.api.unification.recipe.RecipeUnifier;
import com.almostreliable.unified.api.unification.recipe.RecipeUnifierRegistry;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class RecipeUnifierRegistryImpl implements RecipeUnifierRegistry {

    private final Map<ResourceLocation, RecipeUnifier> recipeUnifiersByRecipeType = new HashMap<>();
    private final Map<String, RecipeUnifier> recipeUnifiersByModId = new HashMap<>();

    @Override
    public void registerForRecipeType(ResourceLocation recipeType, RecipeUnifier recipeUnifier) {
        recipeUnifiersByRecipeType.put(recipeType, recipeUnifier);
    }

    @Override
    public void registerForModId(String modId, RecipeUnifier recipeUnifier) {
        recipeUnifiersByModId.put(modId, recipeUnifier);
    }

    @Override
    public RecipeUnifier getRecipeUnifier(RecipeData recipeData) {
        var type = recipeData.getType();
        var byType = recipeUnifiersByRecipeType.get(type);
        if (byType != null) {
            return byType;
        }

        var byMod = recipeUnifiersByModId.get(type.getNamespace());
        if (byMod != null) {
            return byMod;
        }

        if (SmithingRecipeUnifier.isApplicable(recipeData)) {
            return SmithingRecipeUnifier.INSTANCE;
        }

        if (ShapedRecipeUnifier.isApplicable(recipeData)) {
            return ShapedRecipeUnifier.INSTANCE;
        }

        return GenericRecipeUnifier.INSTANCE;
    }
}

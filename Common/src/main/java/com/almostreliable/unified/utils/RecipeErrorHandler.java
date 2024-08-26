package com.almostreliable.unified.utils;

import net.minecraft.resources.ResourceLocation;

import com.almostreliable.unified.AlmostUnifiedCommon;
import com.almostreliable.unified.unification.recipe.RecipeTransformer;

import java.util.HashSet;
import java.util.Set;

public final class RecipeErrorHandler {

    private static final Set<ResourceLocation> RECIPES = new HashSet<>();

    private RecipeErrorHandler() {}

    public static void collect(RecipeTransformer.Result result, ResourceLocation recipe) {
        if (result.getUnifiedRecipes().contains(recipe)) {
            RECIPES.add(recipe);
        }
    }

    public static void finish() {
        if (!RECIPES.isEmpty()) {
            AlmostUnifiedCommon.LOGGER.error(
                "The following recipes were unified and threw exceptions when being loaded: {}",
                RECIPES
            );
            AlmostUnifiedCommon.LOGGER.warn(
                "Try to add them to the ignore list and if the problem is gone, report it to the Almost Unified developers."
            );
        }

        RECIPES.clear();
    }
}

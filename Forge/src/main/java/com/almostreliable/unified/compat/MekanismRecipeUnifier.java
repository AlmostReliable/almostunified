package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.RecipeUnifierBuilder;

import java.util.List;

public class MekanismRecipeUnifier implements RecipeUnifier {
    @Override
    public void collectUnifier(RecipeUnifierBuilder builder) {
        List.of(RecipeConstants.MAIN_INPUT, RecipeConstants.ITEM_INPUT)
                .forEach(key -> builder.put(key, (json, ctx) -> ctx.createIngredientReplacement(json)));

        List.of(RecipeConstants.MAIN_OUTPUT, RecipeConstants.ITEM_OUTPUT, RecipeConstants.SECONDARY_OUTPUT)
                .forEach(key -> builder.put(key, (json, ctx) -> ctx.createResultReplacement(json)));
    }
}

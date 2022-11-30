package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.RecipeUnifierBuilder;

public class MekanismRecipeUnifier implements RecipeUnifier {

    @Override
    public void collectUnifier(RecipeUnifierBuilder builder) {
        builder.put(RecipeConstants.MAIN_INPUT, (json, ctx) -> ctx.createIngredientReplacement(json));
        builder.put(RecipeConstants.MAIN_OUTPUT, (json, ctx) -> ctx.createResultReplacement(json));
        builder.put(RecipeConstants.SECONDARY_OUTPUT, (json, ctx) -> ctx.createResultReplacement(json));
    }
}

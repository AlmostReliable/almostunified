package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.RecipeUnifierBuilder;

public class CyclicRecipeUnifier implements RecipeUnifier {

    @Override
    public void collectUnifier(RecipeUnifierBuilder builder) {
        builder.put(RecipeConstants.BONUS, (json, ctx) -> ctx.createResultReplacement(json));
    }
}

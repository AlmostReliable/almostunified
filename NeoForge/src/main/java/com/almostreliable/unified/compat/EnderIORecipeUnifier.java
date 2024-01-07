package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.RecipeUnifierBuilder;

public class EnderIORecipeUnifier implements RecipeUnifier {

    @Override
    public void collectUnifier(RecipeUnifierBuilder builder) {
        // grinding ball
        builder.put(
                RecipeConstants.ITEM,
                (json, ctx) -> ctx.createResultReplacement(json, false, RecipeConstants.ITEM)
        );
    }
}

package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.RecipeUnifierBuilder;

public class AdAstraRecipeUnifier implements RecipeUnifier {
    @Override
    public void collectUnifier(RecipeUnifierBuilder builder) {
        builder.put("output", (json, ctx) -> {
            return ctx.createResultReplacement(json, false, RecipeConstants.ITEM, "id");
        });
    }
}

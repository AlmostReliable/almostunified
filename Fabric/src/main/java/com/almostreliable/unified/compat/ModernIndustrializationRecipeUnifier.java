package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.RecipeUnifierBuilder;

public class ModernIndustrializationRecipeUnifier implements RecipeUnifier {
    @Override
    public void collectUnifier(RecipeUnifierBuilder builder) {
        builder.put("item_inputs", (json, ctx) -> ctx.createIngredientReplacement(json));
        builder.put("item_outputs", (json, ctx) -> ctx.createResultReplacement(json));
    }
}

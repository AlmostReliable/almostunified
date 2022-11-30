package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.RecipeUnifierBuilder;

public class ModernIndustrializationRecipeUnifier implements RecipeUnifier {

    @Override
    public void collectUnifier(RecipeUnifierBuilder builder) {
        builder.put(RecipeConstants.ITEM_INPUTS, (json, ctx) -> ctx.createIngredientReplacement(json));
        builder.put(RecipeConstants.ITEM_OUTPUTS, (json, ctx) -> ctx.createResultReplacement(json));
    }
}

package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.RecipeUnifierBuilder;

public class AlshanexFamiliarsRecipeUnifier implements RecipeUnifier {

    private static final String CENTRAL_ITEM = "central_item";

    @Override
    public void collectUnifier(RecipeUnifierBuilder builder) {
        builder.put(CENTRAL_ITEM, (json, ctx) -> ctx.createIngredientReplacement(json));
        builder.put(RecipeConstants.INPUTS, (json, ctx) -> ctx.createResultReplacement(json, false, RecipeConstants.ITEM));
        builder.put(RecipeConstants.RESULT, (json, ctx) -> ctx.createResultReplacement(json));
    }
}

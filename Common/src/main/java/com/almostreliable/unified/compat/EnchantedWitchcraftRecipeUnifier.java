package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.RecipeUnifierBuilder;

public class EnchantedWitchcraftRecipeUnifier implements RecipeUnifier {

    @Override
    public void collectUnifier(RecipeUnifierBuilder builder) {
        builder.put(
                RecipeConstants.INGREDIENTS,
                (json, ctx) -> ctx.createResultReplacement(json, false, RecipeConstants.ITEM)
        );
        builder.put(
                RecipeConstants.RESULTS,
                (json, ctx) -> ctx.createResultReplacement(json, false, RecipeConstants.ITEM)
        );
        builder.put(
                RecipeConstants.RESULT,
                (json, ctx) -> ctx.createResultReplacement(json, false, RecipeConstants.ITEM)
        );
    }
}

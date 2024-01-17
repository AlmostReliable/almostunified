package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeContext;
import com.almostreliable.unified.api.recipe.RecipeJson;
import com.almostreliable.unified.api.recipe.RecipeUnifier;

public class CyclicRecipeUnifier implements RecipeUnifier {

    @Override
    public void unifyItems(RecipeContext context, RecipeJson recipe) {
        context.unifyOutputs(recipe, RecipeConstants.BONUS);
    }
}

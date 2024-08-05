package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeJson;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.UnificationHelper;

public class CyclicRecipeUnifier implements RecipeUnifier {

    private static final String BONUS = "bonus";

    @Override
    public void unify(UnificationHelper helper, RecipeJson recipe) {
        helper.unifyOutputs(recipe, BONUS);
    }
}

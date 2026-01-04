package com.almostreliable.unified.compat.unification;

import com.almostreliable.unified.api.unification.recipe.RecipeJson;
import com.almostreliable.unified.api.unification.recipe.RecipeUnifier;
import com.almostreliable.unified.api.unification.recipe.UnificationHelper;

public class CyclicRecipeUnifier implements RecipeUnifier {

    private static final String BONUS = "bonus";

    @Override
    public void unify(UnificationHelper helper, RecipeJson recipe) {
        helper.unifyOutputs(recipe, BONUS);
    }
}

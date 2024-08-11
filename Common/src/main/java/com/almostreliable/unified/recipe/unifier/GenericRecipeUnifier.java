package com.almostreliable.unified.recipe.unifier;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeJson;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.UnificationHelper;

public class GenericRecipeUnifier implements RecipeUnifier {

    public static final GenericRecipeUnifier INSTANCE = new GenericRecipeUnifier();

    @Override
    public void unify(UnificationHelper helper, RecipeJson recipe) {
        unifyInputs(helper, recipe);
        unifyOutputs(helper, recipe);
    }

    public void unifyInputs(UnificationHelper helper, RecipeJson recipe) {
        helper.unifyInputs(recipe, RecipeConstants.DEFAULT_INPUT_KEYS);
    }

    public void unifyOutputs(UnificationHelper helper, RecipeJson recipe) {
        helper.unifyOutputs(recipe, RecipeConstants.DEFAULT_OUTPUT_KEYS);
    }
}

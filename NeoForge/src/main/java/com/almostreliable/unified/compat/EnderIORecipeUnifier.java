package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeJson;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.UnificationHelper;

public class EnderIORecipeUnifier implements RecipeUnifier {

    @Override
    public void unify(UnificationHelper helper, RecipeJson recipe) {
        helper.unifyOutputs(recipe, RecipeConstants.ITEM);
    }
}

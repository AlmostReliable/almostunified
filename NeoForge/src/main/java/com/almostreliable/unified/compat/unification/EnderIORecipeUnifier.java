package com.almostreliable.unified.compat.unification;

import com.almostreliable.unified.api.constant.RecipeConstants;
import com.almostreliable.unified.api.unification.recipe.RecipeJson;
import com.almostreliable.unified.api.unification.recipe.RecipeUnifier;
import com.almostreliable.unified.api.unification.recipe.UnificationHelper;

public class EnderIORecipeUnifier implements RecipeUnifier {

    @Override
    public void unify(UnificationHelper helper, RecipeJson recipe) {
        helper.unifyOutputs(recipe, RecipeConstants.ITEM);
    }
}

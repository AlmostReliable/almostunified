package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.GenericRecipeUnifier;
import com.almostreliable.unified.api.recipe.RecipeJson;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.UnificationHelper;

public final class ProductiveTreesRecipeUnifier implements RecipeUnifier {

    private static final String LEAF_A = "leafA";
    private static final String LEAF_B = "leafB";
    private static final String SECONDARY = "secondary";

    @Override
    public void unify(UnificationHelper helper, RecipeJson recipe) {
        GenericRecipeUnifier.INSTANCE.unify(helper, recipe);
        helper.unifyInputs(recipe, LEAF_A, LEAF_B);
        helper.unifyOutputs(recipe, SECONDARY);
    }
}

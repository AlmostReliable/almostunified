package com.almostreliable.unified.compat.unification;

import com.almostreliable.unified.api.unification.bundled.GenericRecipeUnifier;
import com.almostreliable.unified.api.unification.recipe.RecipeJson;
import com.almostreliable.unified.api.unification.recipe.RecipeUnifier;
import com.almostreliable.unified.api.unification.recipe.UnificationHelper;

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

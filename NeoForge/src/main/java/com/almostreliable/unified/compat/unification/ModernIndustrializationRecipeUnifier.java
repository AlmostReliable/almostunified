package com.almostreliable.unified.compat.unification;

import com.almostreliable.unified.api.unification.bundled.GenericRecipeUnifier;
import com.almostreliable.unified.api.unification.recipe.RecipeJson;
import com.almostreliable.unified.api.unification.recipe.RecipeUnifier;
import com.almostreliable.unified.api.unification.recipe.UnificationHelper;

public class ModernIndustrializationRecipeUnifier implements RecipeUnifier {

    private static final String ITEM_INPUTS = "item_inputs";
    private static final String ITEM_OUTPUTS = "item_outputs";

    @Override
    public void unify(UnificationHelper helper, RecipeJson recipe) {
        GenericRecipeUnifier.INSTANCE.unify(helper, recipe);
        helper.unifyInputs(recipe, ITEM_INPUTS);
        helper.unifyOutputs(recipe, ITEM_OUTPUTS);
    }
}

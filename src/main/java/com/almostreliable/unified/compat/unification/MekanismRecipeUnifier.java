package com.almostreliable.unified.compat.unification;

import com.almostreliable.unified.api.unification.bundled.GenericRecipeUnifier;
import com.almostreliable.unified.api.unification.recipe.RecipeJson;
import com.almostreliable.unified.api.unification.recipe.RecipeUnifier;
import com.almostreliable.unified.api.unification.recipe.UnificationHelper;

public class MekanismRecipeUnifier implements RecipeUnifier {

    private static final String MAIN_INPUT = "main_input";
    private static final String ITEM_INPUT = "item_input";
    private static final String EXTRA_INPUT = "extra_input";
    private static final String MAIN_OUTPUT = "main_output";
    private static final String ITEM_OUTPUT = "item_output";
    private static final String SECONDARY_OUTPUT = "secondary_output";

    @Override
    public void unify(UnificationHelper helper, RecipeJson recipe) {
        GenericRecipeUnifier.INSTANCE.unify(helper, recipe);

        helper.unifyInputs(recipe, MAIN_INPUT);
        helper.unifyInputs(recipe, ITEM_INPUT);
        helper.unifyInputs(recipe, EXTRA_INPUT);

        helper.unifyOutputs(recipe, MAIN_OUTPUT);
        helper.unifyOutputs(recipe, ITEM_OUTPUT);
        helper.unifyOutputs(recipe, SECONDARY_OUTPUT);
    }
}

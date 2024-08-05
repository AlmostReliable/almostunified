package com.almostreliable.unified.recipe.unifier;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeJson;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.UnificationHelper;

import java.util.Set;

public class GenericRecipeUnifier implements RecipeUnifier {
    public static final GenericRecipeUnifier INSTANCE = new GenericRecipeUnifier();
    private static final Set<String> INPUT_KEYS = Set.of(
            RecipeConstants.INPUT,
            RecipeConstants.INPUTS,
            RecipeConstants.INGREDIENT,
            RecipeConstants.INGREDIENTS,
            RecipeConstants.INPUT_ITEMS
    );
    private static final Set<String> OUTPUT_KEYS = Set.of(
            RecipeConstants.OUTPUT,
            RecipeConstants.OUTPUTS,
            RecipeConstants.RESULT,
            RecipeConstants.RESULTS,
            RecipeConstants.OUTPUT_ITEMS
    );

    @Override
    public void unify(UnificationHelper helper, RecipeJson recipe) {
        unifyInputs(helper, recipe);
        unifyOutputs(helper, recipe);
    }

    public void unifyInputs(UnificationHelper helper, RecipeJson recipe) {
        helper.unifyInputs(recipe, INPUT_KEYS);
    }

    public void unifyOutputs(UnificationHelper helper, RecipeJson recipe) {
        helper.unifyOutputs(recipe, OUTPUT_KEYS);
    }
}

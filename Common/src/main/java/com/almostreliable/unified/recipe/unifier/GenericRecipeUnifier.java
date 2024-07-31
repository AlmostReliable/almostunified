package com.almostreliable.unified.recipe.unifier;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeContext;
import com.almostreliable.unified.api.recipe.RecipeJson;
import com.almostreliable.unified.api.recipe.RecipeUnifier;

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
    public void unify(RecipeContext context, RecipeJson recipe) {
        unifyInputs(context, recipe);
        unifyOutputs(context, recipe);
    }

    public void unifyInputs(RecipeContext context, RecipeJson recipe) {
        context.unifyInputs(recipe, INPUT_KEYS);
    }

    public void unifyOutputs(RecipeContext context, RecipeJson recipe) {
        context.unifyOutputs(recipe, OUTPUT_KEYS);
    }
}

package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeContext;
import com.almostreliable.unified.api.recipe.RecipeJson;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.recipe.unifier.GenericRecipeUnifier;

public class MekanismRecipeUnifier implements RecipeUnifier {

    private static final String MAIN_INPUT = "mainInput";
    private static final String ITEM_INPUT = "itemInput";
    private static final String EXTRA_INPUT = "extraInput";
    private static final String MAIN_OUTPUT = "mainOutput";
    private static final String ITEM_OUTPUT = "itemOutput";
    private static final String SECONDARY_OUTPUT = "secondaryOutput";

    @Override
    public void unify(RecipeContext context, RecipeJson recipe) {
        GenericRecipeUnifier.INSTANCE.unify(context, recipe);

        context.unifyInputs(recipe, MAIN_INPUT);
        context.unifyInputs(recipe, ITEM_INPUT);
        context.unifyInputs(recipe, EXTRA_INPUT);

        context.unifyOutputs(recipe, MAIN_OUTPUT);
        context.unifyOutputs(recipe, ITEM_OUTPUT);
        context.unifyOutputs(recipe, SECONDARY_OUTPUT);
    }
}

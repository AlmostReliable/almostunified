package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeContext;
import com.almostreliable.unified.api.recipe.RecipeJson;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.recipe.unifier.GenericRecipeUnifier;

public class ModernIndustrializationRecipeUnifier implements RecipeUnifier {

    private static final String ITEM_INPUTS = "item_inputs";
    private static final String ITEM_OUTPUTS = "item_outputs";

    @Override
    public void unify(RecipeContext context, RecipeJson recipe) {
        GenericRecipeUnifier.INSTANCE.unify(context, recipe);
        context.unifyInputs(recipe, ITEM_INPUTS);
        context.unifyOutputs(recipe, ITEM_OUTPUTS);
    }
}

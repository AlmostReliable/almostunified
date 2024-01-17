package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeContext;
import com.almostreliable.unified.api.recipe.RecipeJson;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.recipe.unifier.GenericRecipeUnifier;

public class MekanismRecipeUnifier implements RecipeUnifier {

    @Override
    public void unifyItems(RecipeContext context, RecipeJson recipe) {
        GenericRecipeUnifier.INSTANCE.unifyItems(context, recipe);

        context.unifyInputs(recipe, RecipeConstants.MAIN_INPUT);
        context.unifyInputs(recipe, RecipeConstants.ITEM_INPUT);
        context.unifyInputs(recipe, RecipeConstants.EXTRA_INPUT);

        context.unifyOutputs(recipe, RecipeConstants.MAIN_OUTPUT);
        context.unifyOutputs(recipe, RecipeConstants.ITEM_OUTPUT);
        context.unifyOutputs(recipe, RecipeConstants.SECONDARY_OUTPUT);
    }
}

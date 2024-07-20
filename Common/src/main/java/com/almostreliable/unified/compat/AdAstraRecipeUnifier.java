package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeContext;
import com.almostreliable.unified.api.recipe.RecipeJson;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.recipe.unifier.GenericRecipeUnifier;

public class AdAstraRecipeUnifier implements RecipeUnifier {

    @Override
    public void unify(RecipeContext context, RecipeJson recipe) {
        GenericRecipeUnifier.INSTANCE.unify(context, recipe);
        context.unifyOutputs(recipe, RecipeConstants.RESULT, false, RecipeConstants.ITEM, "id");
    }
}

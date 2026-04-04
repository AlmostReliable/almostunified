package com.almostreliable.unified.compat.unification;

import com.almostreliable.unified.api.unification.bundled.GenericRecipeUnifier;
import com.almostreliable.unified.api.unification.bundled.ShapedRecipeUnifier;
import com.almostreliable.unified.api.unification.recipe.RecipeJson;
import com.almostreliable.unified.api.unification.recipe.RecipeUnifier;
import com.almostreliable.unified.api.unification.recipe.UnificationHelper;
import com.almostreliable.unified.unification.recipe.RecipeJsonImpl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class CreateRecipeUnifier implements RecipeUnifier {

    private static final String SEQUENCE = "sequence";

    @Override
    public void unify(UnificationHelper helper, RecipeJson recipe) {
        // inherited crafting recipes
        if (ShapedRecipeUnifier.isApplicable(recipe)) {
            ShapedRecipeUnifier.INSTANCE.unify(helper, recipe);
            return;
        }

        // sequenced assembly recipes
        unifySequencedAssemblyRecipes(helper, recipe);

        // all other recipes
        GenericRecipeUnifier.INSTANCE.unify(helper, recipe);
    }

    private void unifySequencedAssemblyRecipes(UnificationHelper helper, RecipeJson recipe) {
        if (!(recipe.getProperty(SEQUENCE) instanceof JsonArray sequenceArray)) {
            return;
        }

        for (var sequenceStep : sequenceArray) {
            if (!(sequenceStep instanceof JsonObject sequenceStepObject)) {
                continue;
            }

            var wrappedRecipe = new RecipeJsonImpl(recipe.getId(), sequenceStepObject);
            GenericRecipeUnifier.INSTANCE.unify(helper, wrappedRecipe);
        }
    }
}

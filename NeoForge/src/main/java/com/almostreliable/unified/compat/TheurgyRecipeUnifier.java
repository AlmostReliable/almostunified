package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeContext;
import com.almostreliable.unified.api.recipe.RecipeJson;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.recipe.unifier.GenericRecipeUnifier;
import com.google.gson.JsonObject;

import java.util.List;

public class TheurgyRecipeUnifier implements RecipeUnifier {

    private static final String MERCURY = "mercury";
    private static final String SALT = "salt";
    private static final String SOLUTE = "solute";
    private static final String SOURCES = "sources";
    private static final String SULFUR = "sulfur";
    private static final String TARGET = "target";

    @Override
    public void unify(RecipeContext context, RecipeJson recipe) {
        GenericRecipeUnifier.INSTANCE.unifyInputs(context, recipe);
        context.unifyInputs(recipe, List.of(MERCURY, SALT, SOLUTE, SOURCES, SULFUR, TARGET));

        if (recipe.getProperty(RecipeConstants.RESULT) instanceof JsonObject result &&
            result.has(OccultismRecipeUnifier.TYPE)) {
            OccultismRecipeUnifier.unifyTypedOutput(context, recipe, result);
            return;
        }

        GenericRecipeUnifier.INSTANCE.unifyOutputs(context, recipe);
    }
}

package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeJson;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.UnificationHelper;
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
    public void unify(UnificationHelper helper, RecipeJson recipe) {
        GenericRecipeUnifier.INSTANCE.unifyInputs(helper, recipe);
        helper.unifyInputs(recipe, List.of(MERCURY, SALT, SOLUTE, SOURCES, SULFUR, TARGET));

        if (recipe.getProperty(RecipeConstants.RESULT) instanceof JsonObject result &&
            result.has(OccultismRecipeUnifier.TYPE)) {
            OccultismRecipeUnifier.unifyTypedOutput(helper, recipe, result);
            return;
        }

        GenericRecipeUnifier.INSTANCE.unifyOutputs(helper, recipe);
    }
}

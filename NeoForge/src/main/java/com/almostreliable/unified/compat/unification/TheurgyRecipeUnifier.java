package com.almostreliable.unified.compat.unification;

import com.almostreliable.unified.api.constant.RecipeConstants;
import com.almostreliable.unified.api.unification.bundled.GenericRecipeUnifier;
import com.almostreliable.unified.api.unification.recipe.RecipeJson;
import com.almostreliable.unified.api.unification.recipe.RecipeUnifier;
import com.almostreliable.unified.api.unification.recipe.UnificationHelper;
import com.google.gson.JsonObject;

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
        helper.unifyInputs(recipe, MERCURY, SALT, SOLUTE, SOURCES, SULFUR, TARGET);

        if (recipe.getProperty(RecipeConstants.RESULT) instanceof JsonObject result &&
            result.has(OccultismRecipeUnifier.TYPE)) {
            OccultismRecipeUnifier.unifyTypedOutput(helper, recipe, result);
            return;
        }

        GenericRecipeUnifier.INSTANCE.unifyOutputs(helper, recipe);
    }
}

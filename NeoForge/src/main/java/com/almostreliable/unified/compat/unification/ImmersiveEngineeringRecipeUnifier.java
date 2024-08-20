package com.almostreliable.unified.compat.unification;

import com.almostreliable.unified.api.constant.RecipeConstants;
import com.almostreliable.unified.api.unification.bundled.GenericRecipeUnifier;
import com.almostreliable.unified.api.unification.recipe.RecipeJson;
import com.almostreliable.unified.api.unification.recipe.RecipeUnifier;
import com.almostreliable.unified.api.unification.recipe.UnificationHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;

public class ImmersiveEngineeringRecipeUnifier implements RecipeUnifier {

    private static final String INPUT_0 = "input0";
    private static final String INPUT_1 = "input1";
    private static final String ADDITIVES = "additives";
    private static final String BASE_INGREDIENT = "base_ingredient";
    private static final String SECONDARIES = "secondaries";
    private static final String SLAG = "slag";

    @Override
    public void unify(UnificationHelper helper, RecipeJson recipe) {
        GenericRecipeUnifier.INSTANCE.unify(helper, recipe);

        List.of(
                // alloy recipes, refinery
                INPUT_0,
                INPUT_1,
                // arc furnace, squeezer, cloche, coke oven, fermenter, fertilizer, metal_press
                RecipeConstants.INPUT,
                // arc furnace
                ADDITIVES,
                // refinery
                RecipeConstants.CATALYST
        ).forEach(key -> unifyInputs(helper, recipe, key));

        List.of(
                RecipeConstants.RESULT,
                RecipeConstants.RESULTS,
                // arc furnace
                SLAG
        ).forEach(key -> helper.unifyOutputs(recipe, key, true, RecipeConstants.ITEM, BASE_INGREDIENT));

        unifySecondaries(helper, recipe);
    }

    public void unifyInputs(UnificationHelper helper, RecipeJson recipe, String key) {
        if (recipe.getProperty(key) instanceof JsonObject json && json.has(BASE_INGREDIENT)) {
            if (helper.unifyInputElement(json.get(BASE_INGREDIENT))) {
                return;
            }
        }

        helper.unifyInputs(recipe, key);
    }

    public void unifySecondaries(UnificationHelper helper, RecipeJson recipe) {
        JsonElement secondaries = recipe.getProperty(SECONDARIES);
        if (secondaries == null) {
            return;
        }

        helper.unifyOutputArray(secondaries.getAsJsonArray(), true, RecipeConstants.OUTPUT);
    }
}

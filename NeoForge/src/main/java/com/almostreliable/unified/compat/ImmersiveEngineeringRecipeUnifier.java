package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeContext;
import com.almostreliable.unified.api.recipe.RecipeJson;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.recipe.unifier.GenericRecipeUnifier;
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
    public void unify(RecipeContext context, RecipeJson recipe) {
        GenericRecipeUnifier.INSTANCE.unify(context, recipe);

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
        ).forEach(key -> unifyInputs(context, recipe, key));

        List.of(
                RecipeConstants.RESULT,
                RecipeConstants.RESULTS,
                // arc furnace
                SLAG
        ).forEach(key -> context.unifyOutputs(recipe, key, true, RecipeConstants.ITEM, BASE_INGREDIENT));

        unifySecondaries(context, recipe);
    }

    public void unifyInputs(RecipeContext context, RecipeJson recipe, String key) {
        if (recipe.getProperty(key) instanceof JsonObject json && json.has(BASE_INGREDIENT)) {
            if (context.unifyBasicInput(json.get(BASE_INGREDIENT))) {
                return;
            }
        }

        context.unifyInputs(recipe, key);
    }

    public void unifySecondaries(RecipeContext context, RecipeJson recipe) {
        JsonElement secondaries = recipe.getProperty(SECONDARIES);
        if (secondaries == null) {
            return;
        }

        context.unifyBasicOutput(secondaries.getAsJsonArray(), true, RecipeConstants.OUTPUT);
    }
}

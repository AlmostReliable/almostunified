package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeContext;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.RecipeUnifierBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.util.List;

public class ImmersiveEngineeringRecipeUnifier implements RecipeUnifier {

    private static final String BASE_INGREDIENT = "base_ingredient";

    @Override
    public void collectUnifier(RecipeUnifierBuilder builder) {
        List.of(
                // alloy recipes, refinery
                RecipeConstants.INPUT_0,
                RecipeConstants.INPUT_1,
                // arc furnace, squeezer, cloche, coke oven, fermenter, fertilizer, metal_press
                RecipeConstants.INPUT,
                // arc furnace
                RecipeConstants.ADDITIVES,
                // refinery
                RecipeConstants.CATALYST
        ).forEach(key -> builder.put(key, this::createIngredientReplacement));

        List.of(
                RecipeConstants.RESULT,
                RecipeConstants.RESULTS,
                // arc furnace
                RecipeConstants.SLAG
        ).forEach(key -> builder.put(key, (json, ctx) ->
                ctx.createResultReplacement(json, true, RecipeConstants.ITEM, BASE_INGREDIENT))
        );

        // alloy recipes, crusher
        builder.forEachObject(RecipeConstants.SECONDARIES, (json, ctx) -> {
                    var replacement = ctx.createResultReplacement(json.get(RecipeConstants.OUTPUT), true);
                    if (replacement instanceof JsonObject output) {
                        json.add(RecipeConstants.OUTPUT, output);
                        return json;
                    }
                    // noinspection ReturnOfNull
                    return null;
                }
        );
    }

    @Nullable
    private JsonElement createIngredientReplacement(@Nullable JsonElement element, RecipeContext ctx) {
        if (element instanceof JsonObject json && json.has(BASE_INGREDIENT)) {
            return ctx.createIngredientReplacement(json.get(BASE_INGREDIENT));
        }

        return ctx.createIngredientReplacement(element);
    }
}

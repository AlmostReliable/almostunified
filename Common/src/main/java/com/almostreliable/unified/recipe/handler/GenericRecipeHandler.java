package com.almostreliable.unified.recipe.handler;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeHandler;
import com.almostreliable.unified.api.recipe.RecipeTransformations;

import java.util.Set;

public class GenericRecipeHandler implements RecipeHandler {
    public static final GenericRecipeHandler INSTANCE = new GenericRecipeHandler();
    private final Set<String> inputKeys = Set.of(RecipeConstants.INPUT,
            RecipeConstants.INGREDIENT,
            RecipeConstants.INGREDIENTS);
    private final Set<String> outputKeys = Set.of(RecipeConstants.OUTPUT,
            RecipeConstants.RESULT,
            RecipeConstants.RESULTS);

    @Override
    public void collectTransformations(RecipeTransformations builder) {
        for (String inputKey : inputKeys) {
            builder.put(inputKey, (json, ctx) -> ctx.createIngredientReplacement(json));
        }

        for (String outputKey : outputKeys) {
            builder.put(outputKey, (json, ctx) -> ctx.createResultReplacement(json));
        }
    }
}

package com.almostreliable.unified.handler;

import com.almostreliable.unified.api.RecipeHandler;
import com.almostreliable.unified.api.RecipeTransformations;

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
            builder.replaceIngredient(inputKey);
        }

        for (String outputKey : outputKeys) {
            builder.replaceResult(outputKey);
        }
    }
}

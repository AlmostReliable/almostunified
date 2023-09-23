package com.almostreliable.unified.recipe.unifier;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.RecipeUnifierBuilder;

import java.util.Set;

public class GenericRecipeUnifier implements RecipeUnifier {
    public static final GenericRecipeUnifier INSTANCE = new GenericRecipeUnifier();
    private static final Set<String> INPUT_KEYS = Set.of(
            RecipeConstants.INPUT,
            RecipeConstants.INPUTS,
            RecipeConstants.INGREDIENT,
            RecipeConstants.INGREDIENTS,
            RecipeConstants.INPUT_ITEMS
    );
    private static final Set<String> OUTPUT_KEYS = Set.of(
            RecipeConstants.OUTPUT,
            RecipeConstants.OUTPUTS,
            RecipeConstants.RESULT,
            RecipeConstants.RESULTS,
            RecipeConstants.OUTPUT_ITEMS
    );

    @Override
    public void collectUnifier(RecipeUnifierBuilder builder) {
        for (String inputKey : INPUT_KEYS) {
            builder.put(inputKey, (json, ctx) -> ctx.createIngredientReplacement(json));
        }

        for (String outputKey : OUTPUT_KEYS) {
            builder.put(outputKey, (json, ctx) -> ctx.createResultReplacement(json));
        }
    }
}

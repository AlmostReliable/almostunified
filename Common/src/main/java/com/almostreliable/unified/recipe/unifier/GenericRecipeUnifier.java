package com.almostreliable.unified.recipe.unifier;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.RecipeUnifierBuilder;

import java.util.Set;

public class GenericRecipeUnifier implements RecipeUnifier {
    public static final GenericRecipeUnifier INSTANCE = new GenericRecipeUnifier();
    private final Set<String> inputKeys = Set.of(RecipeConstants.INPUT,
            RecipeConstants.INGREDIENT,
            RecipeConstants.INGREDIENTS,
            RecipeConstants.INPUT_ITEMS);
    private final Set<String> outputKeys = Set.of(RecipeConstants.OUTPUT,
            RecipeConstants.RESULT,
            RecipeConstants.RESULTS,
            RecipeConstants.OUTPUT_ITEMS);

    @Override
    public void collectUnifier(RecipeUnifierBuilder builder) {
        for (String inputKey : inputKeys) {
            builder.put(inputKey, (json, ctx) -> ctx.createIngredientReplacement(json));
        }

        for (String outputKey : outputKeys) {
            builder.put(outputKey, (json, ctx) -> ctx.createResultReplacement(json));
        }
    }
}

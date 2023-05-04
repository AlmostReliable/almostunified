package com.almostreliable.unified.recipe.unifier;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.RecipeUnifierBuilder;

public class SmithingRecipeUnifier implements RecipeUnifier {

    public static final RecipeUnifier INSTANCE = new SmithingRecipeUnifier();

    public static final String ADDITION_PROPERTY = "addition";
    public static final String BASE_PROPERTY = "base";

    @Override
    public void collectUnifier(RecipeUnifierBuilder builder) {
        builder.put(ADDITION_PROPERTY, (json, ctx) -> ctx.createIngredientReplacement(json));
        builder.put(BASE_PROPERTY, (json, ctx) -> ctx.createIngredientReplacement(json));
        builder.put(RecipeConstants.RESULT, (json, ctx) -> ctx.createResultReplacement(json));
    }
}

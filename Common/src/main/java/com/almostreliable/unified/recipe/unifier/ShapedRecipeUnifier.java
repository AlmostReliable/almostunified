package com.almostreliable.unified.recipe.unifier;

import com.almostreliable.unified.api.recipe.RecipeContext;
import com.almostreliable.unified.api.recipe.RecipeJson;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.google.gson.JsonObject;

public class ShapedRecipeUnifier implements RecipeUnifier {
    public static final RecipeUnifier INSTANCE = new ShapedRecipeUnifier();
    public static final String PATTERN_PROPERTY = "pattern";
    public static final String KEY_PROPERTY = "key";

    @Override
    public void unifyItems(RecipeContext context, RecipeJson recipe) {
        GenericRecipeUnifier.INSTANCE.unifyItems(context, recipe);

        if (recipe.getProperty(KEY_PROPERTY) instanceof JsonObject json) {
            for (var e : json.entrySet()) {
                context.unifyBasicInput(e.getValue());
            }
        }
    }
}

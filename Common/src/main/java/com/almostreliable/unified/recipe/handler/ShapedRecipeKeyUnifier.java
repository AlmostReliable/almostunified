package com.almostreliable.unified.recipe.handler;

import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.RecipeUnifierBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ShapedRecipeKeyUnifier implements RecipeUnifier {
    public static final RecipeUnifier INSTANCE = new ShapedRecipeKeyUnifier();
    public static final String PATTERN_PROPERTY = "pattern";
    public static final String KEY_PROPERTY = "key";

    @Override
    public void collectUnifier(RecipeUnifierBuilder builder) {
        builder.put(KEY_PROPERTY, JsonObject.class, (json, context) -> {
            for (var entry : json.entrySet()) {
                JsonElement result = context.createIngredientReplacement(entry.getValue());
                if (result != null) {
                    entry.setValue(result);
                }
            }
            return json;
        });
    }
}

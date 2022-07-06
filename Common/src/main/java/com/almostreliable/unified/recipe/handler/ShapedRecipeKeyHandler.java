package com.almostreliable.unified.recipe.handler;

import com.almostreliable.unified.api.recipe.RecipeHandler;
import com.almostreliable.unified.api.recipe.RecipeTransformationBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ShapedRecipeKeyHandler implements RecipeHandler {
    public static final RecipeHandler INSTANCE = new ShapedRecipeKeyHandler();
    public static final String PATTERN_PROPERTY = "pattern";
    public static final String KEY_PROPERTY = "key";

    @Override
    public void collectTransformations(RecipeTransformationBuilder builder) {
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

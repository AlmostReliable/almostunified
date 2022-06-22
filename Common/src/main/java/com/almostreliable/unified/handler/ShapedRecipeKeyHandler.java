package com.almostreliable.unified.handler;

import com.almostreliable.unified.api.RecipeHandler;
import com.almostreliable.unified.api.RecipeTransformations;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ShapedRecipeKeyHandler implements RecipeHandler {
    public static final RecipeHandler INSTANCE = new ShapedRecipeKeyHandler();
    public static final String PATTERN_PROPERTY = "pattern";
    public static final String KEY_PROPERTY = "key";

    @Override
    public void collectTransformations(RecipeTransformations builder) {
        builder.put(KEY_PROPERTY, JsonObject.class, (json, context) -> {
            for (var entry : json.entrySet()) {
                JsonElement result = context.replaceIngredient(entry.getValue());
                if (result != null) {
                    entry.setValue(result);
                }
            }
            return json;
        });
    }
}

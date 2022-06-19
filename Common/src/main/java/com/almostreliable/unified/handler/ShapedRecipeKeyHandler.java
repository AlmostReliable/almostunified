package com.almostreliable.unified.handler;

import com.almostreliable.unified.api.RecipeContext;
import com.almostreliable.unified.api.RecipeHandler;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ShapedRecipeKeyHandler implements RecipeHandler {
    public static final RecipeHandler INSTANCE = new ShapedRecipeKeyHandler();
    public static final String PATTERN_PROPERTY = "pattern";
    public static final String KEY_PROPERTY = "key";

    @Override
    public void transformRecipe(JsonObject json, RecipeContext context) {
        if (json.get(KEY_PROPERTY) instanceof JsonObject object) {
            for (var entry : object.entrySet()) {
                context.replaceIngredient(entry.getValue());
            }
        }

        JsonElement result = json.get("result");
        if (result != null) {
            context.replaceResult(result);
        }
    }
}

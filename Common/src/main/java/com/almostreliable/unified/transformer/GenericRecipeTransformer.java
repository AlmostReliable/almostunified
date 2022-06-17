package com.almostreliable.unified.transformer;

import com.almostreliable.unified.api.RecipeTransformer;
import com.almostreliable.unified.api.RecipeTransformContext;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;

public class GenericRecipeTransformer implements RecipeTransformer {
    protected void replaceItems(JsonElement element, RecipeTransformContext context) {
        if (element instanceof JsonObject asObject) {
            if (asObject.has("item")) {
                String newItem = context.findReplacement(asObject.get("item").getAsString());
                if (newItem != null) {
                    asObject.addProperty("item", newItem);
                }
            }
        } else if (element instanceof JsonArray asArray) {
            for (JsonElement innerElement : asArray) {
                replaceItems(innerElement, context);
            }
        }
    }

    @Nullable
    @Override
    public JsonElement transformRecipe(JsonElement json, RecipeTransformContext context) {
        // TODO refactor
        replaceItems(json, context);
        return json;
    }
}

package com.almostreliable.unified.transformer;

import com.almostreliable.unified.api.RecipeTransformer;
import com.almostreliable.unified.api.ReplacementLookupHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;

public class GenericRecipeTransformer implements RecipeTransformer {


    protected void replaceItems(JsonElement element, ReplacementLookupHelper lookup) {
        if (element instanceof JsonObject asObject) {
            if (asObject.has("item")) {
                String newItem = lookup.findReplacement(asObject.get("item").getAsString());
                if (newItem != null) {
                    asObject.addProperty("item", newItem);
                }
            }
        } else if (element instanceof JsonArray asArray) {
            for (JsonElement innerElement : asArray) {
                replaceItems(innerElement, lookup);
            }
        }
    }

    @Nullable
    @Override
    public JsonElement transformRecipe(JsonElement json, ReplacementLookupHelper helper) {
        // TODO refactor
        replaceItems(json, helper);
        return json;
    }
}

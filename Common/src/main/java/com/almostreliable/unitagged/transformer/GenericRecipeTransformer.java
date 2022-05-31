package com.almostreliable.unitagged.transformer;

import com.almostreliable.unitagged.api.RecipeTransformer;
import com.almostreliable.unitagged.api.ReplacementLookupHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.util.List;

public class GenericRecipeTransformer implements RecipeTransformer {

    private final List<String> inputKeys = List.of("input", "ingredient", "ingredients");
    private final List<String> outputKeys = List.of("output", "result", "results");

    @Override
    public void transformRecipe(JsonObject json, ReplacementLookupHelper helper) {
//        if (inputKey != null) {
//            JsonElement element = json.get(inputKey);
//            replaceItems(element, lookup);
//        }
//
//        if(outputKey != null) {
//            JsonElement element = json.get(outputKey);
//            replaceItems(element, lookup);
//        }
    }

    @Nullable
    protected String findKey(List<String> keys, JsonObject json) {
        for (var key : keys) {
            if (json.has(key)) {
                return key;
            }
        }
        return null;
    }

    protected void replaceItems(JsonElement element, ReplacementLookupHelper lookup) {
        if (element instanceof JsonObject asObject) {
            if (asObject.has("item")) {
                String newItem = lookup.findReplacement(asObject.get("item").getAsString());
                if(newItem != null) {
                    asObject.addProperty("item", newItem);
                }
            }
        } else if (element instanceof JsonArray asArray) {
            for (JsonElement innerElement : asArray) {
                replaceItems(innerElement, lookup);
            }
        }
    }
}

package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeContext;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.RecipeUnifierBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import javax.annotation.Nullable;

public class IntegratedDynamicsRecipeUnifier implements RecipeUnifier {

    private static final String ITEMS = "items";

    @Override
    public void collectUnifier(RecipeUnifierBuilder builder) {
        builder.put(RecipeConstants.ITEM, this::createItemReplacement);
        builder.put(RecipeConstants.RESULT, this::createResultReplacement);
    }

    @Nullable
    private JsonElement createItemReplacement(@Nullable JsonElement json, RecipeContext ctx) {
        if (json instanceof JsonPrimitive jsonPrimitive) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add(RecipeConstants.ITEM, jsonPrimitive);
            return ctx.createIngredientReplacement(jsonObject);
        }

        return ctx.createIngredientReplacement(json);
    }

    @Nullable
    private JsonElement createResultReplacement(@Nullable JsonElement json, RecipeContext ctx) {
        if (json instanceof JsonObject jsonObject && jsonObject.has(ITEMS)) {
            JsonElement resultReplacement = ctx.createResultReplacement(jsonObject.get(ITEMS));
            if (resultReplacement != null) {
                jsonObject.add(ITEMS, resultReplacement);
                return jsonObject;
            }
        }

        return null;
    }
}

package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeContext;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.RecipeUnifierBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public class GregTechModernRecipeUnifier implements RecipeUnifier {

    private static final String CONTENT = "content";

    @Override
    public void collectUnifier(RecipeUnifierBuilder builder) {
        List.of(
                RecipeConstants.INPUTS,
                RecipeConstants.TICK_INPUTS
        ).forEach(key ->
                builder.put(key, (json, ctx) -> createContentReplacement(json, ctx, ctx::createIngredientReplacement))
        );

        List.of(
                RecipeConstants.OUTPUTS,
                RecipeConstants.TICK_OUTPUTS
        ).forEach(key ->
                builder.put(key, (json, ctx) -> createContentReplacement(json, ctx, ctx::createResultReplacement))
        );
    }

    @Nullable
    private JsonElement createContentReplacement(@Nullable JsonElement json, RecipeContext ctx, Function<JsonElement, JsonElement> elementTransformer) {
        if (json instanceof JsonObject jsonObject &&
            jsonObject.get(RecipeConstants.ITEM) instanceof JsonArray jsonArray) {
            JsonArray result = new JsonArray();
            boolean changed = false;

            for (JsonElement element : jsonArray) {
                if (element instanceof JsonObject elementObject) {
                    JsonElement replacement = elementTransformer.apply(elementObject.get(CONTENT));
                    if (replacement != null) {
                        elementObject.add(CONTENT, replacement);
                        changed = true;
                    }
                    result.add(elementObject);
                }
            }

            if (changed) {
                jsonObject.add(RecipeConstants.ITEM, result);
                return jsonObject;
            }
        }

        return null;
    }
}

package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeContext;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.RecipeUnifierBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.util.List;

public class TerraFirmaCraftRecipeUnifier implements RecipeUnifier {

    @Override
    public void collectUnifier(RecipeUnifierBuilder builder) {
        // regular ingredient
        List.of(
                RecipeConstants.FIRST_INPUT,
                RecipeConstants.SECOND_INPUT,
                RecipeConstants.INPUT_ITEM,
                RecipeConstants.BATCH
        ).forEach(key -> builder.put(key, (json, ctx) -> ctx.createIngredientReplacement(json)));

        // array of regular ingredients
        builder.forEachObject(RecipeConstants.EXTRA_PRODUCTS, this::createIngredientReplacement);

        // output keys that may contain "stack"
        List.of(
                RecipeConstants.RESULT,
                RecipeConstants.OUTPUT_ITEM,
                RecipeConstants.EXTRA_DROP,
                RecipeConstants.RESULT_ITEM
        ).forEach(key -> builder.put(key, this::createResultReplacement));
    }

    @Nullable
    private JsonElement createResultReplacement(@Nullable JsonElement element, RecipeContext ctx) {
        if (element instanceof JsonObject json && json.has(RecipeConstants.STACK)) {
            return ctx.createResultReplacement(json.get(RecipeConstants.STACK));
        }
        return ctx.createResultReplacement(element);
    }

    @Nullable
    private JsonObject createIngredientReplacement(JsonObject json, RecipeContext ctx) {
        var replacement = ctx.createIngredientReplacement(json.get(RecipeConstants.ITEM));
        if (replacement instanceof JsonObject item) {
            json.add(RecipeConstants.ITEM, item);
            return json;
        }
        return null;
    }
}

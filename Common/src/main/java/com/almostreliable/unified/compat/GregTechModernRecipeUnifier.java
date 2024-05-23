package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeContext;
import com.almostreliable.unified.api.recipe.RecipeJson;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.recipe.unifier.GenericRecipeUnifier;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.Consumer;

public class GregTechModernRecipeUnifier implements RecipeUnifier {

    private static final String CONTENT = "content";

    @Override
    public void unifyItems(RecipeContext context, RecipeJson recipe) {
        GenericRecipeUnifier.INSTANCE.unifyItems(context, recipe);

        doUnify(recipe, RecipeConstants.INPUTS, context::unifyBasicInput);
        doUnify(recipe, RecipeConstants.TICK_INPUTS, context::unifyBasicInput);

        doUnify(recipe,
                RecipeConstants.OUTPUTS,
                json -> context.unifyBasicOutput(json, true, RecipeConstants.ITEM, RecipeConstants.INGREDIENT));
        doUnify(recipe,
                RecipeConstants.TICK_OUTPUTS,
                json -> context.unifyBasicOutput(json, true, RecipeConstants.ITEM, RecipeConstants.INGREDIENT));
    }

    public void doUnify(RecipeJson recipe, String key, Consumer<JsonObject> callback) {
        JsonElement property = recipe.getProperty(key);
        if (property == null) {
            return;
        }

        if (!(property.getAsJsonObject().get(RecipeConstants.ITEM) instanceof JsonArray arr)) {
            return;
        }

        for (JsonElement element : arr) {
            if (element.getAsJsonObject().get(CONTENT) instanceof JsonObject content) {
                callback.accept(content);
            }
        }
    }
}

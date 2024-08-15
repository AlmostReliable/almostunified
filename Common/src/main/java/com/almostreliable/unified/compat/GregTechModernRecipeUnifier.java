package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.Consumer;

public class GregTechModernRecipeUnifier implements RecipeUnifier {

    private static final String TICK_INPUTS = "tickInputs";
    private static final String TICK_OUTPUTS = "tickOutputs";
    private static final String CONTENT = "content";

    @Override
    public void unify(UnificationHelper helper, RecipeJson recipe) {
        GenericRecipeUnifier.INSTANCE.unify(helper, recipe);

        doUnify(recipe, RecipeConstants.INPUTS, helper::unifyInputElement);
        doUnify(recipe, TICK_INPUTS, helper::unifyInputElement);

        doUnify(recipe,
                RecipeConstants.OUTPUTS,
                json -> helper.unifyOutputObject(json, true, RecipeConstants.ITEM, RecipeConstants.INGREDIENT));
        doUnify(recipe,
                TICK_OUTPUTS,
                json -> helper.unifyOutputObject(json, true, RecipeConstants.ITEM, RecipeConstants.INGREDIENT));
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

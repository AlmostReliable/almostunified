package com.almostreliable.unified.compat.unification;

import com.almostreliable.unified.api.constant.RecipeConstants;
import com.almostreliable.unified.api.unification.bundled.GenericRecipeUnifier;
import com.almostreliable.unified.api.unification.recipe.RecipeJson;
import com.almostreliable.unified.api.unification.recipe.RecipeUnifier;
import com.almostreliable.unified.api.unification.recipe.UnificationHelper;

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

    private void doUnify(RecipeJson recipe, String key, Consumer<JsonObject> callback) {
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

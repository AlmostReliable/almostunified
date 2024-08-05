package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeJson;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.UnificationHelper;
import com.almostreliable.unified.recipe.unifier.GenericRecipeUnifier;
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

        doUnify(recipe, RecipeConstants.INPUTS, helper::unifyBasicInput);
        doUnify(recipe, TICK_INPUTS, helper::unifyBasicInput);

        doUnify(recipe,
                RecipeConstants.OUTPUTS,
                json -> helper.unifyBasicOutput(json, true, RecipeConstants.ITEM, RecipeConstants.INGREDIENT));
        doUnify(recipe,
                TICK_OUTPUTS,
                json -> helper.unifyBasicOutput(json, true, RecipeConstants.ITEM, RecipeConstants.INGREDIENT));
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

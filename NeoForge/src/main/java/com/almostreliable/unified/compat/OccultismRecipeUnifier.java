package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeContext;
import com.almostreliable.unified.api.recipe.RecipeJson;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.recipe.unifier.GenericRecipeUnifier;
import com.google.gson.JsonObject;

import java.util.List;

public class OccultismRecipeUnifier implements RecipeUnifier {

    static final String TYPE = "type";
    private static final String ACTIVATION_ITEM = "activation_item";
    private static final String ITEM_TO_USE = "item_to_use";
    private static final String STACK = "stack";

    @Override
    public void unify(RecipeContext context, RecipeJson recipe) {
        GenericRecipeUnifier.INSTANCE.unifyInputs(context, recipe);
        context.unifyInputs(recipe, List.of(ACTIVATION_ITEM, ITEM_TO_USE));

        if (recipe.getProperty(RecipeConstants.RESULT) instanceof JsonObject result && result.has(TYPE)) {
            unifyTypedOutput(context, recipe, result);
            return;
        }

        GenericRecipeUnifier.INSTANCE.unifyOutputs(context, recipe);
    }

    static void unifyTypedOutput(RecipeContext context, RecipeJson recipe, JsonObject result) {
        context.unifyOutputs(recipe, RecipeConstants.RESULT, true, STACK, RecipeConstants.ID);

        // check if the type is a tag but the entry was converted to an item
        var type = result.get(TYPE).getAsString();
        if (!type.contains(RecipeConstants.TAG) || result.has(RecipeConstants.TAG)) return;

        // use correct type: weighted_tag -> weighted_item, tag -> item
        result.addProperty(TYPE, type.replace(RecipeConstants.TAG, RecipeConstants.ITEM));

        // wrap weighted tag values in json object to make it a valid weighted item
        if (type.contains("weighted_tag")) {
            JsonObject stack = new JsonObject();
            stack.add("count", result.get("count"));
            stack.add(RecipeConstants.ID, result.get(RecipeConstants.ID));
            result.add(STACK, stack);
            result.remove("count");
            result.remove(RecipeConstants.ID);
        }
    }
}

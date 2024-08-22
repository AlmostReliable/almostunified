package com.almostreliable.unified.compat.unification;

import com.almostreliable.unified.api.constant.RecipeConstants;
import com.almostreliable.unified.api.unification.bundled.GenericRecipeUnifier;
import com.almostreliable.unified.api.unification.recipe.RecipeJson;
import com.almostreliable.unified.api.unification.recipe.RecipeUnifier;
import com.almostreliable.unified.api.unification.recipe.UnificationHelper;
import com.google.gson.JsonObject;

public class OccultismRecipeUnifier implements RecipeUnifier {

    static final String TYPE = "type";
    private static final String ACTIVATION_ITEM = "activation_item";
    private static final String ITEM_TO_USE = "item_to_use";
    private static final String STACK = "stack";

    @Override
    public void unify(UnificationHelper helper, RecipeJson recipe) {
        GenericRecipeUnifier.INSTANCE.unifyInputs(helper, recipe);
        helper.unifyInputs(recipe, ACTIVATION_ITEM, ITEM_TO_USE);

        if (recipe.getProperty(RecipeConstants.RESULT) instanceof JsonObject result && result.has(TYPE)) {
            unifyTypedOutput(helper, recipe, result);
            return;
        }

        GenericRecipeUnifier.INSTANCE.unifyOutputs(helper, recipe);
    }

    static void unifyTypedOutput(UnificationHelper helper, RecipeJson recipe, JsonObject result) {
        helper.unifyOutputs(recipe, RecipeConstants.RESULT, true, STACK, RecipeConstants.ID);

        // check if the type is a tag but the entry was converted to an item
        var type = result.get(TYPE).getAsString();
        if (!type.contains(RecipeConstants.TAG) || result.has(RecipeConstants.TAG)) return;

        // replace the item key with id
        result.add(RecipeConstants.ID, result.get(RecipeConstants.ITEM));
        result.remove(RecipeConstants.ITEM);

        // use correct type: weighted_tag → weighted_item, tag → item
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

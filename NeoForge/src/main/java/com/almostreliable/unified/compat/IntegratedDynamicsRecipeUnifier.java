package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeJson;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.UnificationHelper;
import com.almostreliable.unified.recipe.unifier.GenericRecipeUnifier;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class IntegratedDynamicsRecipeUnifier implements RecipeUnifier {

    private static final String ITEMS = "items";

    @Override
    public void unify(UnificationHelper helper, RecipeJson recipe) {
        GenericRecipeUnifier.INSTANCE.unify(helper, recipe);

        unifyItemInput(helper, recipe);
        helper.unifyOutputs(recipe, RecipeConstants.RESULT, true, ITEMS);
        unifyItemResult(helper, recipe);
    }

    /**
     * Integrated dynamics allows primitive values for `item` keys in their recipes.
     * AlmostUnified will convert them into JsonObject so that they can be unified to use tags.
     *
     * @param helper the unification helper
     * @param recipe the recipe
     */
    private static void unifyItemInput(UnificationHelper helper, RecipeJson recipe) {
        JsonElement element = recipe.getProperty(RecipeConstants.ITEM);
        if (element instanceof JsonPrimitive primitive) {
            JsonObject itemAsObject = new JsonObject();
            itemAsObject.add(RecipeConstants.ITEM, primitive);
            if (helper.unifyInputElement(itemAsObject)) {
                recipe.setProperty(RecipeConstants.ITEM, itemAsObject);
                return;
            }
        }

        helper.unifyInputs(recipe, RecipeConstants.ITEM);
    }

    private void unifyItemResult(UnificationHelper helper, RecipeJson recipe) {
        JsonElement element = recipe.getProperty(RecipeConstants.RESULT);
        if (!(element instanceof JsonObject result)) {
            return;
        }

        var itemsElement = result.get(ITEMS);
        if (!(itemsElement instanceof JsonArray items)) {
            return;
        }

        helper.unifyOutputArray(items, true, RecipeConstants.ITEM);
    }
}

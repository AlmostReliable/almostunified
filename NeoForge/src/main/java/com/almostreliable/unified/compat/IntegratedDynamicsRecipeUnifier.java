package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeContext;
import com.almostreliable.unified.api.recipe.RecipeJson;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.recipe.unifier.GenericRecipeUnifier;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class IntegratedDynamicsRecipeUnifier implements RecipeUnifier {

    private static final String ITEMS = "items";

    @Override
    public void unifyItems(RecipeContext context, RecipeJson recipe) {
        GenericRecipeUnifier.INSTANCE.unifyItems(context, recipe);

        unifyItemInput(context, recipe);
        context.unifyOutputs(recipe, RecipeConstants.RESULT, true, ITEMS);
        unifyItemResult(context, recipe);
    }

    /**
     * Integrated dynamics allows primitive values for `item` keys in their recipes.
     * AlmostUnified will convert them into JsonObject so that they can be unified to use tags.
     *
     * @param context the recipe context
     * @param recipe  the recipe
     */
    private static void unifyItemInput(RecipeContext context, RecipeJson recipe) {
        JsonElement element = recipe.getProperty(RecipeConstants.ITEM);
        if (element instanceof JsonPrimitive primitive) {
            JsonObject itemAsObject = new JsonObject();
            itemAsObject.add(RecipeConstants.ITEM, primitive);
            if (context.unifyBasicInput(itemAsObject)) {
                recipe.setProperty(RecipeConstants.ITEM, itemAsObject);
                return;
            }
        }

        context.unifyInputs(recipe, RecipeConstants.ITEM);
    }

    private void unifyItemResult(RecipeContext context, RecipeJson recipe) {
        JsonElement element = recipe.getProperty(RecipeConstants.RESULT);
        if (!(element instanceof JsonObject result)) {
            return;
        }

        var itemsElement = result.get(ITEMS);
        if (!(itemsElement instanceof JsonArray items)) {
            return;
        }

        context.unifyBasicOutput(items, true, RecipeConstants.ITEM);
    }
}

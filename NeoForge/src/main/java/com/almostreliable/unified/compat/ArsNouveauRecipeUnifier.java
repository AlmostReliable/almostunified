package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeContext;
import com.almostreliable.unified.api.recipe.RecipeJson;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.recipe.unifier.GenericRecipeUnifier;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ArsNouveauRecipeUnifier implements RecipeUnifier {

    @Override
    public void unifyItems(RecipeContext context, RecipeJson recipe) {
        GenericRecipeUnifier.INSTANCE.unifyItems(context, recipe);
        context.unifyInputs(recipe, RecipeConstants.REAGENT);
        unifyItemInputs(context, recipe, RecipeConstants.PEDESTAL_ITEMS);
        unifyItemInputs(context, recipe, RecipeConstants.INPUT_ITEMS);

    }

    public void unifyItemInputs(RecipeContext context, RecipeJson recipe, String key) {
        if (!(recipe.getProperty(key) instanceof JsonArray array)) {
            return;
        }

        for (JsonElement element : array) {
            if (context.unifyBasicInput(element)) {
                continue;
            }

            if (element instanceof JsonObject obj) {
                JsonElement inner = obj.get(RecipeConstants.ITEM);
                context.unifyBasicInput(inner);
            }
        }
    }
}

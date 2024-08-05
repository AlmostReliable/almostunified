package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeJson;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.UnificationHelper;
import com.almostreliable.unified.recipe.unifier.GenericRecipeUnifier;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ArsNouveauRecipeUnifier implements RecipeUnifier {

    private static final String PEDESTAL_ITEMS = "pedestalItems";
    private static final String REAGENT = "reagent";

    @Override
    public void unify(UnificationHelper helper, RecipeJson recipe) {
        GenericRecipeUnifier.INSTANCE.unify(helper, recipe);
        helper.unifyInputs(recipe, REAGENT);
        unifyItemInputs(helper, recipe, PEDESTAL_ITEMS);
        unifyItemInputs(helper, recipe, RecipeConstants.INPUT_ITEMS);
    }

    public void unifyItemInputs(UnificationHelper helper, RecipeJson recipe, String key) {
        if (!(recipe.getProperty(key) instanceof JsonArray array)) {
            return;
        }

        for (JsonElement element : array) {
            if (helper.unifyBasicInput(element)) {
                continue;
            }

            if (element instanceof JsonObject obj) {
                JsonElement inner = obj.get(RecipeConstants.ITEM);
                helper.unifyBasicInput(inner);
            }
        }
    }
}

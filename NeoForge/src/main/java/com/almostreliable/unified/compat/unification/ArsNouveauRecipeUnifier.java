package com.almostreliable.unified.compat.unification;

import com.almostreliable.unified.api.constant.RecipeConstants;
import com.almostreliable.unified.api.unification.bundled.GenericRecipeUnifier;
import com.almostreliable.unified.api.unification.recipe.RecipeJson;
import com.almostreliable.unified.api.unification.recipe.RecipeUnifier;
import com.almostreliable.unified.api.unification.recipe.UnificationHelper;
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
            if (helper.unifyInputElement(element)) {
                continue;
            }

            if (element instanceof JsonObject obj) {
                JsonElement inner = obj.get(RecipeConstants.ITEM);
                helper.unifyInputElement(inner);
            }
        }
    }
}

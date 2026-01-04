package com.almostreliable.unified.compat.unification;

import com.almostreliable.unified.api.constant.RecipeConstants;
import com.almostreliable.unified.api.unification.recipe.CustomIngredientUnifier;
import com.almostreliable.unified.api.unification.recipe.UnificationHelper;

import com.google.gson.JsonObject;

public class CompoundIngredientUnifier implements CustomIngredientUnifier {

    public static final String CHILDREN = "children";

    @Override
    public boolean unify(UnificationHelper helper, JsonObject jsonObject) {
        return helper.unifyInputObject(jsonObject, RecipeConstants.INGREDIENTS, CHILDREN);
    }
}

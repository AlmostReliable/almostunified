package com.almostreliable.unitagged.api;

import com.google.gson.JsonObject;

@FunctionalInterface
public interface RecipeTransformer {

    /**
     * Transforms the input of a recipe.
     *
     * @param json The input of the recipe.
     * @param helper The helper to use for finding replacements.
     */
    void transformRecipe(JsonObject json, ReplacementLookupHelper helper);
}

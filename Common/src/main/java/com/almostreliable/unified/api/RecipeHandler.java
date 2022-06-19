package com.almostreliable.unified.api;

import com.google.gson.JsonObject;

@FunctionalInterface
public interface RecipeHandler {

    void transformRecipe(JsonObject json, RecipeContext context);

    default String getName() {
        return getClass().getSimpleName();
    }
}

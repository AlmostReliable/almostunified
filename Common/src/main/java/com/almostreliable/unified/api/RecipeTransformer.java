package com.almostreliable.unified.api;

import com.google.gson.JsonElement;

import javax.annotation.Nullable;

@FunctionalInterface
public interface RecipeTransformer {
    @Nullable
    JsonElement transformRecipe(JsonElement json, ReplacementLookupHelper helper);
}

package com.almostreliable.unified.api.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.BiFunction;

public interface RecipeUnifierBuilder {

    void forEachObject(String property, BiFunction<JsonObject, RecipeContext, JsonObject> consumer);

    void put(String property, BiFunction<JsonElement, RecipeContext, JsonElement> consumer);

    <T extends JsonElement> void put(String property, Class<T> type, BiFunction<T, RecipeContext, T> consumer);
}

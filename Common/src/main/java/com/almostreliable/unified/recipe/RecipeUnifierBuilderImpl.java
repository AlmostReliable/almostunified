package com.almostreliable.unified.recipe;

import com.almostreliable.unified.api.recipe.RecipeContext;
import com.almostreliable.unified.api.recipe.RecipeUnifierBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class RecipeUnifierBuilderImpl implements RecipeUnifierBuilder {
    private final Map<String, Entry<?>> consumers = new HashMap<>();

    @Override
    public void forEachObject(String property, BiFunction<JsonObject, RecipeContext, JsonObject> consumer) {
        BiFunction<JsonArray, RecipeContext, JsonArray> arrayConsumer = (array, ctx) -> {
            for (int i = 0; i < array.size(); i++) {
                JsonElement element = array.get(i);
                if (element instanceof JsonObject obj) {
                    JsonObject result = consumer.apply(obj, ctx);
                    if (result != null) {
                        array.set(i, result);
                    }
                }
            }
            return array;
        };

        put(property, JsonArray.class, arrayConsumer);
    }

    @Override
    public void put(String property, BiFunction<JsonElement, RecipeContext, JsonElement> consumer) {
        consumers.put(property, new Entry<>(JsonElement.class, consumer));
    }

    @Override
    public <T extends JsonElement> void put(String property, Class<T> type, BiFunction<T, RecipeContext, T> consumer) {
        consumers.put(property, new Entry<>(type, consumer));
    }

    public JsonObject unify(JsonObject json, RecipeContext context) {
        JsonObject changedValues = new JsonObject();

        for (var e : json.entrySet()) {
            Entry<?> consumer = consumers.get(e.getKey());
            if (consumer != null) {
                JsonElement currentElement = e.getValue();
                JsonElement transformedElement = consumer.apply(currentElement.deepCopy(), context);
                if (transformedElement != null && !transformedElement.equals(currentElement)) {
                    changedValues.add(e.getKey(), transformedElement);
                }
            }
        }

        if (changedValues.size() == 0) {
            return null;
        }

        // helps to preserve the order of the elements
        JsonObject result = new JsonObject();
        for (var entry : json.entrySet()) {
            JsonElement changedValue = changedValues.get(entry.getKey());
            if (changedValue != null) {
                result.add(entry.getKey(), changedValue);
            } else {
                result.add(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }

    private record Entry<T extends JsonElement>(Class<T> expectedType,
                                                BiFunction<T, RecipeContext, T> func) {
        @Nullable
        T apply(JsonElement json, RecipeContext context) {
            if (expectedType.isInstance(json)) {
                return func.apply(expectedType.cast(json), context);
            }

            return null;
        }
    }
}

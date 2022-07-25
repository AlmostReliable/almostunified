package com.almostreliable.unified.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.StreamSupport;

public class JsonUtils {
    public static JsonArray arrayOrSelf(@Nullable JsonElement element) {
        if (element == null) {
            return new JsonArray();
        }

        if (element.isJsonArray()) {
            return element.getAsJsonArray();
        }

        JsonArray array = new JsonArray();
        array.add(element);
        return array;
    }

    public static <T extends JsonElement> void arrayForEach(@Nullable JsonElement element, Class<T> filter, Consumer<T> consumer) {
        for (JsonElement e : arrayOrSelf(element)) {
            if (filter.isInstance(e)) {
                consumer.accept(filter.cast(e));
            }
        }
    }

    /**
     * Loops through the array and applies the given callback to each element.
     * If the callback returns non-null, the element is replaced with the returned value.
     *
     * @param jsonArray The array to loop through.
     * @param callback  The callback to apply to each element.
     * @return true if any elements were replaced, false otherwise.
     */
    public static boolean replaceOn(JsonArray jsonArray, UnaryOperator<JsonElement> callback) {
        boolean changed = false;
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonElement result = callback.apply(jsonArray.get(i));
            if (result != null) {
                jsonArray.set(i, result);
                changed = true;
            }
        }
        return changed;
    }

    /**
     * Replaces the element for the key through given callback.
     * If the callback returns non-null, the element is replaced with the returned value.
     *
     * @param jsonObject The object to loop through.
     * @param callback   The callback to apply to each element.
     * @return true if the element was replaced, false otherwise.
     */
    public static boolean replaceOn(JsonObject jsonObject, String key, UnaryOperator<JsonElement> callback) {
        JsonElement element = jsonObject.get(key);
        if (element == null) {
            return false;
        }

        JsonElement result = callback.apply(element);
        if (result != null) {
            jsonObject.add(key, result);
            return true;
        }
        return false;
    }

    public static JsonArray toArray(List<String> list) {
        JsonArray array = new JsonArray();
        list.forEach(array::add);
        return array;
    }

    public static List<String> toList(JsonArray array) {
        return StreamSupport.stream(array.spliterator(), false).map(JsonElement::getAsString).toList();
    }
}

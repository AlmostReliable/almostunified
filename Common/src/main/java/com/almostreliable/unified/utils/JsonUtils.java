package com.almostreliable.unified.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import javax.annotation.Nullable;
import java.util.function.Consumer;

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
}

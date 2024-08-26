package com.almostreliable.unified.utils;

import com.google.gson.*;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class JsonUtils {

    private static final Gson GSON = new Gson();

    private JsonUtils() {}

    public static <T extends JsonElement> T readFromFile(Path path, Class<T> clazz) throws IOException {
        BufferedReader reader = Files.newBufferedReader(path);
        return GSON.fromJson(reader, clazz);
    }

    public static <T extends JsonElement> T safeReadFromFile(Path path, T defaultValue) {
        try {
            return readFromFile(path, Utils.cast(defaultValue.getClass()));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static <T extends JsonElement> T readFromString(String jsonString, Class<T> clazz) {
        return GSON.fromJson(jsonString, clazz);
    }

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

    public static JsonObject objectOrSelf(@Nullable JsonElement element) {
        if (element instanceof JsonObject jsonObject) {
            return jsonObject;
        }
        return new JsonObject();
    }

    public static String stringOrSelf(@Nullable JsonElement element) {
        if (element instanceof JsonPrimitive primitive) {
            return primitive.getAsString();
        }
        return "";
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

    public static JsonArray toArray(Iterable<String> list) {
        JsonArray array = new JsonArray();
        list.forEach(array::add);
        return array;
    }

    public static List<String> toList(JsonArray array) {
        return StreamSupport.stream(array.spliterator(), false).map(JsonElement::getAsString).toList();
    }

    public static <K, V> Map<K, V> deserializeMap(
        JsonObject json,
        String key,
        Function<Map.Entry<String, JsonElement>, K> keyMapper,
        Function<Map.Entry<String, JsonElement>, V> valueMapper
    ) {
        return json.getAsJsonObject(key)
            .entrySet()
            .stream()
            .collect(Collectors.toMap(keyMapper, valueMapper, (a, b) -> b, HashMap::new));
    }

    public static <K, V> Map<K, Set<V>> deserializeMapSet(
        JsonObject json,
        String key,
        Function<Map.Entry<String, JsonElement>, K> keyMapper,
        Function<String, V> valueMapper
    ) {
        return deserializeMap(
            json,
            key,
            keyMapper,
            e -> toList(e.getValue().getAsJsonArray())
                .stream()
                .map(valueMapper)
                .collect(Collectors.toSet())
        );
    }
}

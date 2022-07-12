package com.almostreliable.unified.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

public class JsonCompare {

    public static int compare(JsonObject first, JsonObject second, LinkedHashMap<String, Rule> rules) {
        for (var entry : rules.entrySet()) {
            JsonElement fElement = first.get(entry.getKey());
            JsonElement sElement = second.get(entry.getKey());

            if (fElement == null && sElement == null) {
                continue;
            }

            int compareIndex = entry.getValue().compare(fElement, sElement);
            if (compareIndex != 0) {
                return compareIndex;
            }
        }
        return 0;
    }

    public static JsonObject compare(LinkedHashMap<String, Rule> rules, JsonObject... jsonObjects) {
        List<JsonObject> unsorted = Arrays.asList(jsonObjects);
        unsorted.sort((f, s) -> compare(f, s, rules));
        return unsorted.get(0);
    }

    public static boolean matches(JsonObject first, JsonObject second, Collection<String> ignoredProperties) {
        List<String> firstValidKeys = first
                .keySet()
                .stream()
                .filter(key -> !ignoredProperties.contains(key))
                .toList();
        List<String> secondValidKeys = second
                .keySet()
                .stream()
                .filter(key -> !ignoredProperties.contains(key))
                .toList();

        if (firstValidKeys.size() != secondValidKeys.size()) return false;

        for (String firstKey : firstValidKeys) {
            if (!first.get(firstKey).equals(second.get(firstKey))) {
                return false;
            }
        }

        return true;
    }

    @FunctionalInterface
    public interface Rule {
        /**
         * Compare two JsonElements. The caller must ensure that at least one element is not null.
         */
        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        int compare(@Nullable JsonElement first, @Nullable JsonElement second);
    }

    public static class LowerRule implements Rule {
        @Override
        public int compare(@Nullable JsonElement first, @Nullable JsonElement second) {
            double firstValue = first instanceof JsonPrimitive fp ? fp.getAsDouble() : 0;
            double secondValue = second instanceof JsonPrimitive sp ? sp.getAsDouble() : 0;
            return Double.compare(firstValue, secondValue);
        }
    }

    public static class HigherRule implements Rule {
        @Override
        public int compare(@Nullable JsonElement first, @Nullable JsonElement second) {
            double firstValue = first instanceof JsonPrimitive fp ? fp.getAsDouble() : 0;
            double secondValue = second instanceof JsonPrimitive sp ? sp.getAsDouble() : 0;
            return Double.compare(secondValue, firstValue);
        }
    }
}

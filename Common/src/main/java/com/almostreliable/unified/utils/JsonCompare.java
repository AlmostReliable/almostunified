package com.almostreliable.unified.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import javax.annotation.Nullable;
import java.util.*;

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

    public interface Rule {
        /**
         * Compare two JsonElements. The caller must ensure that at least one element is not null.
         */
        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        int compare(@Nullable JsonElement first, @Nullable JsonElement second);

        String getName();
    }

    public static class LowerRule implements Rule {
        public static final String NAME = "LowerRule";

        @Override
        public int compare(@Nullable JsonElement first, @Nullable JsonElement second) {
            double firstValue = first instanceof JsonPrimitive fp ? fp.getAsDouble() : 0;
            double secondValue = second instanceof JsonPrimitive sp ? sp.getAsDouble() : 0;
            return Double.compare(firstValue, secondValue);
        }

        @Override
        public String getName() {
            return NAME;
        }
    }

    public static class HigherRule implements Rule {
        public static final String NAME = "HigherRule";

        @Override
        public int compare(@Nullable JsonElement first, @Nullable JsonElement second) {
            double firstValue = first instanceof JsonPrimitive fp ? fp.getAsDouble() : 0;
            double secondValue = second instanceof JsonPrimitive sp ? sp.getAsDouble() : 0;
            return Double.compare(secondValue, firstValue);
        }

        @Override
        public String getName() {
            return NAME;
        }
    }

    public static class CompareSettings {
        public static final String IGNORED_FIELDS = "ignoredFields";
        public static final String RULES = "rules";

        private final LinkedHashMap<String, Rule> rules = new LinkedHashMap<>();
        private final Set<String> ignoredFields = new HashSet<>();

        public void ignoreField(String property) {
            ignoredFields.add(property);
        }

        public void addRule(String key, Rule rule) {
            Rule old = rules.put(key, rule);
            ignoreField(key);
            if (old != null) {
                throw new IllegalStateException("Multiple rule for key <" + key + "> found");
            }
        }

        public Set<String> getIgnoredFields() {
            return Collections.unmodifiableSet(ignoredFields);
        }

        public JsonObject serialize() {
            JsonObject result = new JsonObject();

            JsonArray ignoredFieldsArray = new JsonArray();
            ignoredFields.stream().filter(f -> !rules.containsKey(f)).forEach(ignoredFieldsArray::add);
            result.add(IGNORED_FIELDS, ignoredFieldsArray);

            JsonObject rulesJson = new JsonObject();
            rules.forEach((s, rule) -> {
                rulesJson.addProperty(s, rule.getName());
            });
            result.add(RULES, rulesJson);

            return result;
        }

        public void deserialize(JsonObject json) {
            json.getAsJsonArray(IGNORED_FIELDS).forEach(e -> {
                ignoreField(e.getAsString());
            });

            json.getAsJsonObject(RULES).entrySet().forEach(e -> {
                JsonCompare.Rule r = switch (e.getValue().getAsString()) {
                    case JsonCompare.HigherRule.NAME -> new JsonCompare.HigherRule();
                    case JsonCompare.LowerRule.NAME -> new JsonCompare.LowerRule();
                    default -> throw new IllegalArgumentException("Unknown rule <" + e.getValue().getAsString() + ">");
                };
                addRule(e.getKey(), r);
            });
        }

        public LinkedHashMap<String, Rule> getRules() {
            return rules;
        }
    }
}

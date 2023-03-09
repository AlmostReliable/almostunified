package com.almostreliable.unified.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import javax.annotation.Nullable;
import java.util.*;

public final class JsonCompare {

    private static final Set<String> SANITIZE_KEYS = Set.of("item", "tag", "id");

    private JsonCompare() {}

    public static int compare(JsonObject first, JsonObject second, Map<String, Rule> rules) {
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

    public static JsonObject compare(Map<String, Rule> rules, JsonObject... jsonObjects) {
        List<JsonObject> unsorted = Arrays.asList(jsonObjects);
        unsorted.sort((f, s) -> compare(f, s, rules));
        return unsorted.get(0);
    }

    @Nullable
    public static JsonObject compareShaped(JsonObject first, JsonObject second, CompareSettings compareSettings) {
        if (!matches(first, second, compareSettings)) return null;

        JsonArray firstPattern = JsonUtils.arrayOrSelf(first.get("pattern"));
        JsonArray secondPattern = JsonUtils.arrayOrSelf(second.get("pattern"));

        if (firstPattern.size() != secondPattern.size()) {
            return null;
        }
        for (int i = 0; i < firstPattern.size(); i++) {
            if (JsonUtils.stringOrSelf(firstPattern.get(i)).length() !=
                JsonUtils.stringOrSelf(secondPattern.get(i)).length()) {
                return null;
            }
        }

        var firstKeyMap = createShapedKeyMap(first);
        var secondKeyMap = createShapedKeyMap(second);

        for (int i = 0; i < firstPattern.size(); i++) {
            String firstPatternString = JsonUtils.stringOrSelf(firstPattern.get(i));
            String secondPatternString = JsonUtils.stringOrSelf(secondPattern.get(i));

            for (int j = 0; j < firstPatternString.length(); j++) {
                char firstChar = firstPatternString.charAt(j);
                char secondChar = secondPatternString.charAt(j);
                if (firstChar == ' ' && secondChar == ' ') continue;
                if (!firstKeyMap.containsKey(firstChar) || !secondKeyMap.containsKey(secondChar)) {
                    return null;
                }
                if (!firstKeyMap.get(firstChar).equals(secondKeyMap.get(secondChar))) {
                    return null;
                }
            }
        }

        return first;
    }

    private static Map<Character, JsonObject> createShapedKeyMap(JsonObject json) {
        JsonObject keys = JsonUtils.objectOrSelf(json.get("key"));
        Map<Character, JsonObject> keyMap = new HashMap<>();
        for (Map.Entry<String, JsonElement> patterKey : keys.entrySet()) {
            char c = patterKey.getKey().charAt(0);
            if (c == ' ') continue;
            keyMap.put(c, JsonUtils.objectOrSelf(patterKey.getValue()));
        }
        return keyMap;
    }

    public static boolean matches(JsonObject first, JsonObject second, CompareSettings compareSettings) {
        Collection<String> ignoredFields = compareSettings.getIgnoredFields();
        if(ignoredFields.isEmpty()) {
            // optimization: check if the objects have the same number of keys, since we won't be filtering any
            if(first.size() != second.size())
                return false;
        }

        // iterate using entrySet to avoid needing to call get() on JsonObject (it is an O(log(n)) operation)
        for(Map.Entry<String, JsonElement> firstEntry : first.entrySet()) {
            if(ignoredFields.contains(firstEntry.getKey()))
                continue;

            JsonElement firstElem = firstEntry.getValue();
            JsonElement secondElem = second.get(firstEntry.getKey());

            // if the key doesn't exist on the second element, they are clearly not equal
            if (secondElem == null) return false;

            // sanitize elements for implicit counts of 1
            if (compareSettings.shouldSanitize && needsSanitizing(firstElem, secondElem)) {
                firstElem = sanitize(firstElem);
                secondElem = sanitize(secondElem);
            }

            if (!firstElem.equals(secondElem)) {
                return false;
            }
        }

        return true;
    }

    /**
     * A check whether the given elements need to be sanitized. The purpose of this check is
     * to save performance by skipping pairs that are not affected by sanitizing.
     * <p>
     * Conditions are both elements being a JSON array with the same size, both elements being
     * a JSON object, one element being a JSON object and the other being a JSON primitive.
     * @param firstElem the first element
     * @param secondElem the second element
     * @return true if the elements need to be sanitized, false otherwise
     */
    private static boolean needsSanitizing(JsonElement firstElem, JsonElement secondElem) {
        return (firstElem instanceof JsonArray firstArray && secondElem instanceof JsonArray secondArray &&
                firstArray.size() == secondArray.size()) ||
               (firstElem instanceof JsonObject && secondElem instanceof JsonObject) ||
               (firstElem instanceof JsonPrimitive && secondElem instanceof JsonObject) ||
               (firstElem instanceof JsonObject && secondElem instanceof JsonPrimitive);
    }

    /**
     * Creates a sanitized object from the given element with a count of 1 and the
     * value from the original object under a dummy key called "au_sanitized".
     * <p>
     * If the element is not a string primitive, the default object is returned.
     * @param value The value to sanitize
     * @param defaultValue The default value to return if the element is not a string primitive
     * @return The sanitized object or the default value
     */
    private static JsonElement createSanitizedObjectOrDefault(JsonElement value, JsonElement defaultValue) {
        if (value instanceof JsonPrimitive primitive && primitive.isString()) {
            var newObject = new JsonObject();
            newObject.addProperty("au_sanitized", primitive.getAsString());
            newObject.addProperty("count", 1);
            return newObject;
        }
        return defaultValue;
    }

    /**
     * Used to sanitize root level JSON elements to make them comparable when the count of 1 is implicit.
     * <p>
     * This transforms string primitives, JSON objects and JSON arrays to JSON objects where the
     * count of 1 is explicitly set. The transformation is only applied to a dummy object and not to
     * the original recipe, so it can be safely used for comparison.
     * <p>
     * If the object doesn't support this transformation, the original object is returned.
     * @param element The element to sanitize
     * @return The sanitized element or the original element if it can't be sanitized
     */
    @SuppressWarnings("ChainOfInstanceofChecks")
    private static JsonElement sanitize(JsonElement element) {
        if (element instanceof JsonArray jsonArray) {
            JsonArray newArray = new JsonArray();
            for (JsonElement arrayElement : jsonArray) {
                newArray.add(sanitize(arrayElement));
            }
            return newArray;
        }

        if (element instanceof JsonObject jsonObject) {
            var keySet = jsonObject.keySet();

            if (keySet.stream().filter(SANITIZE_KEYS::contains).count() != 1) {
                return element;
            }

            // if it has a count property, it needs to be 1, otherwise it's implicit 1 as well and needs sanitizing
            if (keySet.contains("count") && JsonQuery.of(jsonObject, "count").asInt().filter(i -> i == 1).isEmpty()) {
                return element;
            }

            var key = keySet.stream().filter(SANITIZE_KEYS::contains).findFirst().orElseThrow();
            var sanitized = createSanitizedObjectOrDefault(jsonObject.get(key), jsonObject);

            // ensure the object changed (was sanitized) and that we got a JsonObject
            //noinspection ObjectEquality
            if (sanitized == jsonObject || !(sanitized instanceof JsonObject sanitizedObject)) {
                return jsonObject;
            }

            mergeRemainingProperties(jsonObject, sanitizedObject);
            return sanitizedObject;
        }

        return createSanitizedObjectOrDefault(element, element);
    }

    /**
     * Merges remaining properties from the original object to the sanitized object.
     * @param jsonObject The original object
     * @param sanitizedObject The sanitized object
     */
    private static void mergeRemainingProperties(JsonObject jsonObject, JsonObject sanitizedObject) {
        for (var entry : jsonObject.entrySet()) {
            if (!SANITIZE_KEYS.contains(entry.getKey()) && !entry.getKey().equals("count")) {
                sanitizedObject.add(entry.getKey(), entry.getValue());
            }
        }
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
        public static final String SHOULD_SANITIZE = "shouldSanitize";

        private final LinkedHashMap<String, Rule> rules = new LinkedHashMap<>();
        private final Set<String> ignoredFields = new HashSet<>();
        private boolean shouldSanitize;

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

        public void setShouldSanitize(boolean shouldSanitize) {
            this.shouldSanitize = shouldSanitize;
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

            result.addProperty(SHOULD_SANITIZE, shouldSanitize);

            return result;
        }

        public void deserialize(JsonObject json) {
            json.getAsJsonArray(IGNORED_FIELDS).forEach(e -> {
                ignoreField(e.getAsString());
            });

            json.getAsJsonObject(RULES).entrySet().forEach(e -> {
                Rule r = switch (e.getValue().getAsString()) {
                    case HigherRule.NAME -> new HigherRule();
                    case LowerRule.NAME -> new LowerRule();
                    default -> throw new IllegalArgumentException("Unknown rule <" + e.getValue().getAsString() + ">");
                };
                addRule(e.getKey(), r);
            });

            shouldSanitize = json.getAsJsonPrimitive(SHOULD_SANITIZE).getAsBoolean();
        }

        public Map<String, Rule> getRules() {
            return rules;
        }
    }
}

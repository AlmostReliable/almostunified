package com.almostreliable.unified.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class JsonQuery {

    @Nullable private final JsonElement element;

    JsonQuery(@Nullable JsonElement element) {
        this.element = element;
    }

    JsonQuery() {
        this.element = null;
    }

    public static JsonQuery of(JsonElement element) {
        return new JsonQuery(element);
    }

    public static JsonQuery of(JsonElement element, String path) {
        String[] parts = path.split("/");
        JsonQuery current = of(element);
        for (String part : parts) {
            if (StringUtils.isNumeric(part)) {
                current = current.get(Integer.parseInt(part));
            } else {
                current = current.get(part);
            }
        }
        return current;
    }

    public JsonQuery get(String identifier) {
        if (element instanceof JsonObject json) {
            JsonElement child = json.get(identifier);
            if (child != null) {
                return new JsonQuery(child);
            }
        }

        return new JsonQuery();
    }

    public JsonQuery get(int index) {
        if (element instanceof JsonArray json && index >= 0 && index < json.size()) {
            JsonElement child = json.get(index);
            if (child != null) {
                return new JsonQuery(child);
            }

        }

        return new JsonQuery();
    }

    public JsonQuery get(String identifier, int index) {
        return get(identifier).get(index);
    }

    public Optional<JsonElement> asElement() {
        return Optional.ofNullable(element);
    }

    public Optional<JsonObject> asObject() {
        return asElement().filter(JsonObject.class::isInstance).map(JsonObject.class::cast);
    }

    public Optional<JsonArray> asArray() {
        return asElement().filter(JsonArray.class::isInstance).map(JsonArray.class::cast);
    }

    public Optional<String> asString() {
        return asElement().filter(JsonElement::isJsonPrimitive).map(JsonElement::getAsString);
    }

    public Optional<Integer> asInt() {
        return asElement().filter(JsonElement::isJsonPrimitive).map(JsonElement::getAsJsonPrimitive)
                .filter(JsonPrimitive::isNumber).map(JsonElement::getAsInt);
    }

    public JsonQuery shallowCopy() {
        if (element == null) {
            return new JsonQuery();
        }

        if (element instanceof JsonObject jsonObject) {
            var copyObject = new JsonObject();
            for (var entry : jsonObject.entrySet()) {
                copyObject.add(entry.getKey(), entry.getValue());
            }
            return new JsonQuery(copyObject);
        }

        throw new UnsupportedOperationException();
    }
}

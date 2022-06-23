package com.almostreliable.unified.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;

public class JsonQuery {

    private final JsonElement element;

    public static JsonQuery of(JsonElement element) {
        return new JsonQuery(element);
    }

    public static JsonQuery of(JsonElement element, String path) {
        String[] parts = path.split("/");
        JsonQuery current = of(element);
        for (String part : parts) {
            if(StringUtils.isNumeric(part)) {
                current = current.get(Integer.parseInt(part));
            } else {
                current = current.get(part);
            }
        }
        return current;
    }

    JsonQuery(JsonElement element) {
        this.element = element;
    }

    public JsonQuery get(String identifier) {
        if(!element.isJsonObject()) {
            throw new IllegalArgumentException("Expected JsonObject, got " + element.getClass());
        }

        JsonElement child = element.getAsJsonObject().get(identifier);
        if(child == null) {
            return null;
        }

        return new JsonQuery(child);
    }

    public JsonQuery get(int index) {
        if(!element.isJsonArray()) {
            throw new IllegalArgumentException("Expected JsonArray, got " + element.getClass());
        }

        JsonElement child = element.getAsJsonArray().get(index);
        if(child == null) {
            return null;
        }

        return new JsonQuery(child);
    }

    public JsonQuery get(String identifier, int index) {
        return get(identifier).get(index);
    }

    public JsonObject asObject() {
        return element.getAsJsonObject();
    }

    public JsonArray asArray() {
        return element.getAsJsonArray();
    }

    public String asString() {
        return element.getAsString();
    }

    public int asInt() {
        return element.getAsInt();
    }

    public boolean asBoolean() {
        return element.getAsBoolean();
    }

    public float asFloat() {
        return element.getAsFloat();
    }
}

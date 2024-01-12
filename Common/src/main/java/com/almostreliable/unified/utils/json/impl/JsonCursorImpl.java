package com.almostreliable.unified.utils.json.impl;

import com.almostreliable.unified.utils.json.JsonCursor;
import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import javax.annotation.Nullable;
import java.util.ConcurrentModificationException;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class JsonCursorImpl implements JsonCursor {

    public static JsonCursorImpl of(JsonObject parent, String key) {
        if (!parent.has(key)) {
            throw new IllegalStateException("Key '" + key + "' does not exist in parent object");
        }

        return new Object(parent, key);
    }

    public static JsonCursorImpl of(JsonArray parent, int index) {
        if (index < 0 || index >= parent.size()) {
            throw new IllegalStateException("Index '" + index + "' does not exist in parent array");
        }

        return new Array(parent, index);
    }

    @Nullable
    protected Runnable onSetListener;

    public JsonCursorImpl setOnSetListener(Runnable onSetListener) {
        this.onSetListener = onSetListener;
        return this;
    }

    @Override
    public boolean isArray() {
        return value().isJsonArray();
    }

    @Override
    public boolean isNull() {
        return value().isJsonNull();
    }

    @Override
    public boolean isObject() {
        return value().isJsonObject();
    }

    @Override
    public boolean isPrimitive() {
        return value().isJsonPrimitive();
    }

    @Override
    public void walk(Consumer<JsonCursor> callback) {
        JsonElement e = value();
        if (e instanceof JsonObject json) {
            for (String k : json.keySet()) {
                callback.accept(new Object(json, k));
            }
        } else if (e instanceof JsonArray arr) {
            for (int i = 0; i < arr.size(); i++) {
                callback.accept(new Array(arr, i));
            }
        }
    }

    @Override
    public Optional<JsonCursor> next(String key) {
        return Optional.empty();
    }

    @Override
    public Optional<JsonCursor> next(int index) {
        return Optional.empty();
    }

    @Override
    public void set(String string) {
        set(new JsonPrimitive(string));
    }

    @Override
    public String valueAsString() {
        return value().getAsString();
    }

    @Override
    public String toString() {
        return value().toString();
    }

    private static class Object extends JsonCursorImpl {

        private final JsonObject parent;
        private final String key;

        public Object(JsonObject parent, String key) {
            this.parent = parent;
            this.key = key;
        }

        @Override
        public void set(JsonElement element) {
            if (onSetListener != null && value() != element) {
                onSetListener.run();
            }

            parent.add(key, element);
        }

        @Override
        public JsonElement value() {
            JsonElement result = parent.get(key);
            if (result == null) {
                throw new ConcurrentModificationException("Parent object in cursor changed during iteration");
            }

            return result;
        }

        @Override
        public Optional<JsonCursor> next(String key) {
            if (value() instanceof JsonObject selfObject) {
                return Optional.of(new Object(selfObject, key));
            }

            return Optional.empty();
        }
    }

    private static class Array extends JsonCursorImpl {

        private final JsonArray parent;
        private final int index;
        private final int modCount;

        private Array(JsonArray parent, int index) {
            Preconditions.checkArgument(index >= 0 && index < parent.size());
            this.parent = parent;
            this.index = index;
            this.modCount = parent.size();
        }

        @Override
        public void set(JsonElement element) {
            if (onSetListener != null && value() != element) {
                onSetListener.run();
            }

            parent.set(index, element);
        }

        @Override
        public JsonElement value() {
            ensureModCount();
            return parent.get(index);
        }

        @Override
        public Optional<JsonCursor> next(int index) {
            if (value() instanceof JsonArray selfArray) {
                return Optional.of(new Array(selfArray, index));
            }

            return Optional.empty();
        }

        private void ensureModCount() {
            if (modCount != parent.size()) {
                throw new ConcurrentModificationException("Array size changed during iteration");
            }
        }
    }
}

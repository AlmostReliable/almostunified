package com.almostreliable.unified.utils.json;

import com.almostreliable.unified.utils.json.impl.JsonCursorImpl;
import com.google.gson.JsonObject;

import java.util.function.Consumer;

public class JsonWalker implements JsonWalkable {

    private final JsonObject json;
    private boolean changed = false;

    public JsonWalker(JsonObject json) {
        this.json = json;
    }

    private void markChanged() {
        changed = true;
    }

    public boolean hasChanged() {
        return changed;
    }

    @Override
    public void walk(Consumer<JsonCursor> callback) {
        for (String key : json.keySet()) {
            callback.accept(JsonCursorImpl.of(json, key).setOnSetListener(this::markChanged));
        }
    }
}

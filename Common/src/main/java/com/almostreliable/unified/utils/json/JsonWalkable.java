package com.almostreliable.unified.utils.json;

import java.util.function.Consumer;

public interface JsonWalkable {

    void walk(Consumer<JsonCursor> callback);
}

package com.almostreliable.unified.utils.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

import java.util.Optional;

/**
 * Represents a cursor into a json tree. Cursors hold their current position inside the json, which makes it possible
 * to change primitive values directly in place.
 * <p>
 * Cursors should only be used temporally and where the pointed json will not be modified from outside.
 * So they don't necessary protect against concurrent modification exceptions.
 * The only exception is when the cursor's parent is an array. Therefore,
 * the cursor requires to check the size of the array. Otherwise, the element is shifted.
 */
public interface JsonCursor extends JsonWalkable {

    /**
     * Will set the element at the current position of the cursor.
     * Passing `null` as value will throw an exception. If you want to explicitly set `null`, use {@link JsonNull}
     *
     * @param element the element to set
     */
    void set(JsonElement element);

    void set(String string);

    /**
     * Will return the element at the current position of the cursor. Null values are returned as {@link JsonNull}
     *
     * @return the element
     */
    JsonElement value();

    String valueAsString();

    boolean isNull();

    boolean isObject();

    boolean isArray();

    boolean isPrimitive();

    Optional<JsonCursor> next(String key);

    Optional<JsonCursor> next(int index);

//    interface Object extends JsonCursor {
//        /**
//         * Returns the next element with the given key
//         *
//         * @param key the key
//         * @return the next element if it exists, otherwise {@link Optional#empty()}
//         */
//        Optional<JsonCursor> next(String key);
//
//        Optional<JsonCursor.Primitive> nextAsPrimitive(String key);
//
//        Optional<JsonCursor.Array> nextAsArray(String key);
//
//        Optional<JsonCursor.Object> nextAsObject(String key);
//
//        void remove(String key);
//
//        void add(String key, JsonElement json);
//
//        void add(String key, String str);
//
//        @Override
//        JsonObject value();
//    }
//
//    interface Array extends JsonCursor {
//        /**
//         * Returns the next element with the given index
//         *
//         * @param index the index
//         * @return the next element if it exists, otherwise {@link Optional#empty()}
//         */
//        Optional<JsonCursor> next(int index);
//
//        Optional<JsonCursor.Primitive> nextAsPrimitive(int index);
//
//        Optional<JsonCursor.Array> nextAsArray(int index);
//
//        Optional<JsonCursor.Object> nextAsObject(int index);
//
//        @Override
//        JsonArray value();
//    }
//
//    interface Primitive extends JsonCursor {
//
//        void set(String str);
//
//        @Override
//        JsonPrimitive value();
//
//        String asString();
//
//    }
}

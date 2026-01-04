package com.almostreliable.unified.api.unification.recipe;

import com.google.gson.JsonElement;

import org.jetbrains.annotations.Nullable;

/**
 * Abstraction of a recipe JSON to access and override properties.
 *
 * @since 1.0.0
 */
public interface RecipeJson extends RecipeData {

    /**
     * Returns the value of the property with the given key.
     *
     * @param key the key to retrieve the property for
     * @return the property value or null if not present
     */
    @Nullable
    JsonElement getProperty(String key);

    /**
     * Sets the property with the given key to the given value.
     *
     * @param key   the key to set the property for
     * @param value the value to set
     */
    void setProperty(String key, JsonElement value);
}

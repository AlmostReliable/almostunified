package com.almostreliable.unified.api.recipe;

import com.google.gson.JsonElement;

import javax.annotation.Nullable;

public interface RecipeJson extends RecipeData {

    boolean changed();

    void markChanged();

    @Nullable
    JsonElement getProperty(String key);

    void setProperty(String key, JsonElement value);
}

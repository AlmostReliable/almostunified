package com.almostreliable.unified.recipe;

import com.almostreliable.unified.api.recipe.RecipeJson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RecipeJsonImpl implements RecipeJson {

    private final ResourceLocation id;
    private final JsonObject json;
    private boolean changed;

    public RecipeJsonImpl(ResourceLocation id, JsonObject json) {
        this.id = id;
        this.json = json;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public ResourceLocation getType() {
        try {
            return new ResourceLocation(json.get("type").getAsString());
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not detect recipe type");
        }
    }

    @Override
    public boolean hasProperty(String property) {
        return json.has(property);
    }

    @Override
    public boolean changed() {
        return changed;
    }

    @Override
    public void markChanged() {
        changed = true;
    }

    @Nullable
    @Override
    public JsonElement getProperty(String key) {
        return json.get(key);
    }

    @Override
    public void setProperty(String key, JsonElement value) {
        Objects.requireNonNull(value, "value cannot be null");
        json.add(key, value);
        markChanged();
    }
}

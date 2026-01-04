package com.almostreliable.unified.unification.recipe;

import net.minecraft.resources.Identifier;

import com.almostreliable.unified.api.unification.recipe.RecipeJson;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class RecipeJsonImpl implements RecipeJson {

    private final Identifier id;
    private final JsonObject json;

    public RecipeJsonImpl(Identifier id, JsonObject json) {
        this.id = id;
        this.json = json;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public Identifier getType() {
        try {
            return Identifier.parse(json.get("type").getAsString());
        } catch (Exception e) {
            throw new IllegalArgumentException("could not detect recipe type for recipe " + id);
        }
    }

    @Override
    public boolean hasProperty(String key) {
        return json.has(key);
    }

    @Override
    public JsonElement getProperty(String key) {
        return json.get(key);
    }

    @Override
    public void setProperty(String key, JsonElement value) {
        Preconditions.checkNotNull(value, "value cannot be null");
        json.add(key, value);
    }
}

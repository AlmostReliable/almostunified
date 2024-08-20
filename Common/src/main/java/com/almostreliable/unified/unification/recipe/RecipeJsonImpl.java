package com.almostreliable.unified.unification.recipe;

import com.almostreliable.unified.api.unification.recipe.RecipeJson;
import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

public class RecipeJsonImpl implements RecipeJson {

    private final ResourceLocation id;
    private final JsonObject json;

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
            return ResourceLocation.parse(json.get("type").getAsString());
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not detect recipe type");
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

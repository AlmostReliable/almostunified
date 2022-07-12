package com.almostreliable.unified.recipe;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Objects;

public class RawRecipe {
    private final ResourceLocation id;
    private final ResourceLocation type;
    private final JsonObject originalRecipe;
    private boolean isDuplicate = false;
    @Nullable private JsonObject transformedRecipe;


    public RawRecipe(ResourceLocation id, JsonObject originalRecipe) {
        this.id = id;
        this.originalRecipe = originalRecipe;

        try {
            this.type = ResourceLocation.tryParse(originalRecipe.get("type").getAsString());
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not detect recipe type");
        }
    }

    public ResourceLocation getId() {
        return id;
    }

    public ResourceLocation getType() {
        return type;
    }

    public JsonObject getOriginal() {
        return originalRecipe;
    }

    public void markAsDuplicate() {
        isDuplicate = true;
    }

    public boolean isDuplicate() {
        return isDuplicate;
    }

    public void setTransformed(JsonObject transformedRecipe) {
        Objects.requireNonNull(transformedRecipe);
        if(isTransformed()) {
            throw new IllegalStateException("Recipe already transformed");
        }

        this.transformedRecipe = transformedRecipe;
    }

    @Nullable
    public JsonObject getTransformed() {
        return transformedRecipe;
    }

    public boolean isTransformed() {
        return transformedRecipe != null;
    }
}

package com.almostreliable.unified.recipe;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.utils.JsonCompare;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public class RawRecipe {
    private final ResourceLocation id;
    private final ResourceLocation type;
    private final JsonObject originalRecipe;
    @Nullable private DuplicateLink duplicateLink;
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

    void linkDuplicate(RawRecipe recipe) {
        Objects.requireNonNull(recipe);
        if(recipe.getDuplicateLink() != null) {
            AlmostUnified.LOG.error("Recipe {} already linked", recipe.getId());
        }

        this.duplicateLink = new DuplicateLink(this);
        this.duplicateLink.addDuplicate(recipe);
    }

    @Nullable
    public DuplicateLink getDuplicateLink() {
        return duplicateLink;
    }

    public void setTransformed(JsonObject transformedRecipe) {
        Objects.requireNonNull(transformedRecipe);
        if (isTransformed()) {
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


    private List<String> getIgnoredFields() {
        return List.of("conditions");
    }

    private LinkedHashMap<String, JsonCompare.Rule> getRules() {
        LinkedHashMap<String, JsonCompare.Rule> rules = new LinkedHashMap<>();
        rules.put("experience", new JsonCompare.HigherRule());
        rules.put("cookingtime", new JsonCompare.LowerRule());
        return rules;
    }

    @Nullable
    public RawRecipe compare(RawRecipe toCompare) {
        JsonObject selfActual = getTransformed() != null ? getTransformed() : originalRecipe;
        JsonObject toCompareActual = toCompare.getTransformed() != null ? toCompare.getTransformed()
                                                                        : toCompare.getOriginal();

        if (JsonCompare.matches(selfActual, toCompareActual, getIgnoredFields())) {
            JsonObject compare = JsonCompare.compare(getRules(), selfActual, toCompareActual);
            if (compare == null) {
                return null;
            }

            if (compare == selfActual) {
                return this;
            }

            if (compare == toCompareActual) {
                return toCompare;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        String duplicate = duplicateLink != null ? " (duplicate)" : "";
        String transformed = transformedRecipe != null ? " (transformed)" : "";
        return String.format("['%s'] %s%s%s", type, id, duplicate, transformed);
    }
}

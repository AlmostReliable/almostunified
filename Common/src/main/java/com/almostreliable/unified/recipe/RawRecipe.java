package com.almostreliable.unified.recipe;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.utils.JsonCompare;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.*;

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

    private void setDuplicateLink(@Nullable DuplicateLink duplicateLink) {
        Objects.requireNonNull(duplicateLink);
        if (hasDuplicateLink()) {
            throw new IllegalStateException("Recipe already linked");
        }

        this.duplicateLink = duplicateLink;
        this.duplicateLink.addDuplicate(this);
    }


    public boolean hasDuplicateLink() {
        return duplicateLink != null;
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

    public boolean handleDuplicate(RawRecipe recipe) {
        if (hasDuplicateLink()) {
            throw new IllegalStateException("Recipe already linked");
        }

        DuplicateLink link = recipe.getDuplicateLink();
        if(link != null) {
            RawRecipe compare = compare(link.getMaster());
            if(compare != null) {
                link.updateMaster(this);
                setDuplicateLink(link);
                return true;
            }
        } else {
            RawRecipe compare = compare(recipe);
            if(compare != null) {
                DuplicateLink newLink = new DuplicateLink(compare);
                setDuplicateLink(newLink);
                recipe.setDuplicateLink(newLink);
                return true;
            }
        }

        return false;
    }

    public static class DuplicateLink {
        private RawRecipe currentMaster;
        private final Set<RawRecipe> recipes = new HashSet<>();

        private DuplicateLink(RawRecipe master) {
            updateMaster(master);
        }

        private void updateMaster(RawRecipe master) {
            Objects.requireNonNull(master);
            addDuplicate(master);
            this.currentMaster = master;
        }

        private void addDuplicate(RawRecipe recipe) {
            recipes.add(recipe);
        }

        public RawRecipe getMaster() {
            return currentMaster;
        }

        public Set<RawRecipe> getRecipes() {
            return Collections.unmodifiableSet(recipes);
        }

        @Override
        public String toString() {
            return "Link{currentMaster=" + currentMaster + ", recipes=" + recipes.size() + "}";
        }
    }
}

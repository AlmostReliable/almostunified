package com.almostreliable.unified.unification.recipe;

import com.almostreliable.unified.api.unification.recipe.RecipeData;
import com.almostreliable.unified.utils.JsonCompare;
import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class RecipeLink implements RecipeData {
    private final ResourceLocation id;
    private final ResourceLocation type;
    private final JsonObject originalRecipe;
    @Nullable private DuplicateLink duplicateLink;
    @Nullable private JsonObject unifiedRecipe;

    public RecipeLink(ResourceLocation id, JsonObject originalRecipe) {
        this.id = id;
        this.originalRecipe = originalRecipe;

        try {
            this.type = ResourceLocation.tryParse(originalRecipe.get("type").getAsString());
        } catch (Exception e) {
            throw new IllegalArgumentException("could not detect recipe type");
        }
    }

    /**
     * Compare two recipes for equality with given rules. Keys from rules will automatically count as ignored field for the base comparison.
     * If base comparison succeed then the recipes will be compared for equality with rules from {@link JsonCompare.Rule}.
     * Rules are sorted, first rule with the highest priority will be used.
     *
     * @param first           first recipe to compare
     * @param second          second recipe to compare
     * @param compareSettings Settings to use for comparison.
     * @return the recipe where rules are applied and the recipes are compared for equality, or null if the recipes are not equal
     */
    @Nullable
    public static RecipeLink compare(RecipeLink first, RecipeLink second, JsonCompare.CompareSettings compareSettings) {
        JsonObject selfActual = first.getActual();
        JsonObject toCompareActual = second.getActual();

        JsonObject compare = null;
        if (first.getType().toString().equals("minecraft:crafting_shaped")) {
            compare = JsonCompare.compareShaped(selfActual, toCompareActual, compareSettings);
        } else if (JsonCompare.matches(selfActual, toCompareActual, compareSettings)) {
            compare = JsonCompare.compare(compareSettings.getRules(), selfActual, toCompareActual);
        }

        if (compare == null) return null;
        if (compare == selfActual) return first;
        if (compare == toCompareActual) return second;
        return null;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public ResourceLocation getType() {
        return type;
    }

    @Override
    public boolean hasProperty(String key) {
        return getOriginal().has(key);
    }

    public JsonObject getOriginal() {
        return originalRecipe;
    }

    public boolean hasDuplicateLink() {
        return duplicateLink != null;
    }

    @Nullable
    public DuplicateLink getDuplicateLink() {
        return duplicateLink;
    }

    private void updateDuplicateLink(@Nullable DuplicateLink duplicateLink) {
        Preconditions.checkNotNull(duplicateLink);
        if (hasDuplicateLink() && getDuplicateLink() != duplicateLink) {
            throw new IllegalStateException("recipe is already linked to " + getDuplicateLink());
        }

        this.duplicateLink = duplicateLink;
        this.duplicateLink.addDuplicate(this);
    }

    @Nullable
    public JsonObject getUnified() {
        return unifiedRecipe;
    }

    public boolean isUnified() {
        return unifiedRecipe != null;
    }

    void setUnified(JsonObject json) {
        Preconditions.checkNotNull(json);
        if (isUnified()) {
            throw new IllegalStateException("recipe already unified");
        }

        this.unifiedRecipe = json;
    }

    @Override
    public String toString() {
        String duplicate = duplicateLink != null ? " (duplicate)" : "";
        String unified = unifiedRecipe != null ? " (unified)" : "";
        return String.format("['%s'] %s%s%s", type, id, duplicate, unified);
    }

    /**
     * Checks for duplicate against given recipe data. If recipe data already has a duplicate link,
     * the master from the link will be used. Otherwise, we will create a new link if needed.
     *
     * @param otherRecipe     Recipe data to check for duplicate against.
     * @param compareSettings Settings to use for comparison.
     * @return True if recipe is a duplicate, false otherwise.
     */
    public boolean handleDuplicate(RecipeLink otherRecipe, JsonCompare.CompareSettings compareSettings) {
        DuplicateLink selfDuplicate = getDuplicateLink();
        DuplicateLink otherDuplicate = otherRecipe.getDuplicateLink();

        if (selfDuplicate != null && otherDuplicate != null) {
            return selfDuplicate == otherDuplicate;
        }

        if (selfDuplicate == null && otherDuplicate == null) {
            RecipeLink compare = compare(this, otherRecipe, compareSettings);
            if (compare == null) {
                return false;
            }

            DuplicateLink newLink = new DuplicateLink(compare);
            updateDuplicateLink(newLink);
            otherRecipe.updateDuplicateLink(newLink);
            return true;
        }

        if (otherDuplicate != null) {
            RecipeLink compare = compare(this, otherDuplicate.getMaster(), compareSettings);
            if (compare == null) {
                return false;
            }
            otherDuplicate.updateMaster(compare);
            updateDuplicateLink(otherDuplicate);
            return true;
        }

        // selfDuplicate != null
        RecipeLink compare = compare(selfDuplicate.getMaster(), otherRecipe, compareSettings);
        if (compare == null) {
            return false;
        }
        selfDuplicate.updateMaster(compare);
        otherRecipe.updateDuplicateLink(selfDuplicate);
        return true;
    }

    public JsonObject getActual() {
        return getUnified() != null ? getUnified() : getOriginal();
    }

    public static final class DuplicateLink {
        private final Set<RecipeLink> recipes = new HashSet<>();
        private RecipeLink currentMaster;

        private DuplicateLink(RecipeLink master) {
            updateMaster(master);
        }

        private void updateMaster(RecipeLink master) {
            Preconditions.checkNotNull(master);
            addDuplicate(master);
            this.currentMaster = master;
        }

        private void addDuplicate(RecipeLink recipe) {
            recipes.add(recipe);
        }

        public RecipeLink getMaster() {
            return currentMaster;
        }

        public Set<RecipeLink> getRecipes() {
            return Collections.unmodifiableSet(recipes);
        }

        public Set<RecipeLink> getRecipesWithoutMaster() {
            return recipes.stream().filter(recipe -> recipe != currentMaster).collect(Collectors.toSet());
        }

        @Override
        public String toString() {
            return "Link{currentMaster=" + currentMaster + ", recipes=" + recipes.size() + "}";
        }
    }
}

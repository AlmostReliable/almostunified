package com.almostreliable.unified.recipe;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class DuplicateLink {
    private RawRecipe currentMaster;
    private final Set<RawRecipe> recipes = new HashSet<>();

    public DuplicateLink(RawRecipe master) {
        updateMaster(master);
    }

    void updateMaster(RawRecipe master) {
        Objects.requireNonNull(master);
        addDuplicate(master);
        this.currentMaster = master;
    }

    void addDuplicate(RawRecipe recipe) {
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

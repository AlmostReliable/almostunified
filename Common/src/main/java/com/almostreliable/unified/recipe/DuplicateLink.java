package com.almostreliable.unified.recipe;

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
        4 = {DuplicateLink@28142}         Objects.requireNonNull(master);
        addDuplicate(master);
        this.currentMaster = master;
    }

    void addDuplicate(RawRecipe recipe) {
        if(recipe == null) {
            String s = "";
        }
        recipes.add(recipe);
    }

    public RawRecipe getMaster() {
        return currentMaster;
    }
}

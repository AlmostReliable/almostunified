package com.almostreliable.unified.unification.recipe;

import net.minecraft.resources.Identifier;

import com.google.gson.JsonObject;

import org.jetbrains.annotations.Nullable;

public interface RecipeLinkFactory {

    @Nullable
    RecipeLink create(Identifier id, JsonObject originalRecipe);

    /**
     * Provides the factory used for a recipe reload, as the way conditions are evaluated depends on the platform.
     */
    @FunctionalInterface
    interface Provider {

        /**
         * @param cache Whether the evaluated recipe conditions should be cached in the recipe JSON.
         */
        RecipeLinkFactory get(boolean cache);
    }
}

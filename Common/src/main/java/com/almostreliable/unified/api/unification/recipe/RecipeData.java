package com.almostreliable.unified.api.unification.recipe;

import net.minecraft.resources.ResourceLocation;

/**
 * Basic information about a recipe used for determination of the correct {@link RecipeUnifier}.
 */
public interface RecipeData {

    /**
     * Returns the recipe id as a {@link ResourceLocation}.
     *
     * @return the id
     */
    ResourceLocation getId();

    /**
     * Returns the recipe type as a {@link ResourceLocation}.
     *
     * @return the recipe type
     */
    ResourceLocation getType();

    /**
     * Checks if the current recipe contains the property with the given key.
     *
     * @param key the key of the property to check for
     * @return true if the recipe contains the property, false otherwise
     */
    boolean hasProperty(String key);
}

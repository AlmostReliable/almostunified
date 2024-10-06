package com.almostreliable.unified.api.unification.recipe;

import net.minecraft.resources.ResourceLocation;

import com.google.gson.JsonObject;

import org.jetbrains.annotations.Nullable;

/**
 * The registry holding all {@link CustomIngredientUnifier}s.
 * <p>
 * {@link CustomIngredientUnifier}s can be registered per type.
 *
 * @since 1.2.0
 */
public interface CustomIngredientUnifierRegistry {

    /**
     * Registers a {@link CustomIngredientUnifier} for a specific type.
     * <p>
     * If a custom ingredient unifier is associated with the given type, the internal transformation for the
     * {@link JsonObject} will be skipped.
     *
     * @param type                    the type to register the custom ingredient unifier for
     * @param customIngredientUnifier the custom ingredient unifier
     */
    void registerForType(ResourceLocation type, CustomIngredientUnifier customIngredientUnifier);

    /**
     * Retrieves the respective {@link CustomIngredientUnifier} for the given type.
     *
     * @param type the type to retrieve the {@link CustomIngredientUnifier} for
     * @return the {@link CustomIngredientUnifier} for the given type
     */
    @Nullable
    CustomIngredientUnifier getCustomIngredientUnifier(ResourceLocation type);
}

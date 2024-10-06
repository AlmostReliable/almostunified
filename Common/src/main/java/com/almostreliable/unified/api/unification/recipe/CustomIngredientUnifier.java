package com.almostreliable.unified.api.unification.recipe;

import com.almostreliable.unified.api.plugin.AlmostUnifiedPlugin;

import com.google.gson.JsonObject;

/**
 * Implemented on custom ingredient unifiers.
 * <p>
 * Custom unifiers will tell Almost Unified how to handle specific ingredients.<br>
 * When the unification process encounters a {@link JsonObject} that contains a {@code type} property, it will check
 * if a custom unifier is associated with that type and call
 * {@link CustomIngredientUnifier#unify(UnificationHelper, JsonObject)}. If no unifier is found, the default
 * transformation will be used.
 * <p>
 * Unifiers can be registered for a specific type. Registering a custom unifier will skip the default transformation
 * of the {@link JsonObject} for the given type.
 * <p>
 * Registration is handled in {@link CustomIngredientUnifierRegistry} which can be obtained in
 * {@link AlmostUnifiedPlugin#registerCustomIngredientUnifiers(CustomIngredientUnifierRegistry)}.
 *
 * @since 1.2.0
 */
public interface CustomIngredientUnifier {

    /**
     * Uses of the given {@link UnificationHelper} to unify the given ingredient {@link JsonObject}.
     * <p>
     * The unifier only receives the serialized ingredient as {@link JsonObject} without the {@code type} property.<br>
     * If changes to the {@link JsonObject} are necessary, the original {@link JsonObject} should be modified in-place.
     * The method should return true if the {@link JsonObject} was modified.
     *
     * @param helper     the helper to aid in the unification
     * @param jsonObject the ingredient to unify as a {@link JsonObject}
     * @return true if the ingredient was changed, false otherwise
     */
    boolean unify(UnificationHelper helper, JsonObject jsonObject);
}

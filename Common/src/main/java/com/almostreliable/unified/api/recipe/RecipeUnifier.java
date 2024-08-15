package com.almostreliable.unified.api.recipe;

import com.almostreliable.unified.api.RecipeUnifierRegistry;
import com.almostreliable.unified.api.plugin.AlmostUnifiedPlugin;

/**
 * Implemented on custom recipe unifiers.
 * <p>
 * Custom unifiers will tell Almost Unified how to handle specific recipes.<br>
 * It can provide information about custom recipe keys not covered by the default unifiers and how to
 * treat them. Whether they support ingredient replacements or just items.<br>
 * Recipes will be marked as modified automatically through comparison with the original JSON.
 * <p>
 * Unifiers can either be registered per recipe type or per mod id. Registering a custom unifier will
 * disable the default unifiers such as the {@link GenericRecipeUnifier}.
 * <p>
 * Registration is handled in {@link RecipeUnifierRegistry} which can be obtained in
 * {@link AlmostUnifiedPlugin#registerRecipeUnifiers(RecipeUnifierRegistry)}.
 */
public interface RecipeUnifier {

    /**
     * Uses of the given {@link UnificationHelper} to unify the given {@link RecipeJson}.
     * <p>
     * {@link RecipeJson} is a utility wrapper that allows to easily access recipe information such as the recipe id,
     * the recipe type and provides methods to check or modify the raw JSON.
     *
     * @param helper the helper to aid in the unification
     * @param recipe the recipe to unify as a {@link RecipeJson}
     */
    void unify(UnificationHelper helper, RecipeJson recipe);
}

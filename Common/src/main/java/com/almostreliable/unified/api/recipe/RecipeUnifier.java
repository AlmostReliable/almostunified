package com.almostreliable.unified.api.recipe;

import com.almostreliable.unified.api.UnifierRegistry;
import com.almostreliable.unified.api.plugin.AlmostUnifiedPlugin;
import com.almostreliable.unified.recipe.unifier.GenericRecipeUnifier;

/**
 * Implemented on custom recipe unifiers.
 * <p>
 * Custom unifiers will tell Almost Unified how to handle specific recipes.<br>
 * It can provide information about custom recipe keys not covered by the default unifiers and how to
 * treat them. Whether they support ingredient replacements or just items.<br>
 * Recipe will be marked as modified automatically through comparison with the original JSON.
 * <p>
 * Unifiers can either be registered per recipe type or per mod id. Registering a custom unifier will
 * disable the default unifiers such as {@link GenericRecipeUnifier}.
 * <p>
 * Registration is handled in {@link UnifierRegistry} which can be obtained in your {@link AlmostUnifiedPlugin}.
 */
public interface RecipeUnifier {

    /**
     * Makes use of the provided {@link RecipeContext} to unify the given {@link RecipeJson}.
     *
     * @param context the {@link RecipeContext} to aid in the unification
     * @param recipe  the recipe to unify as raw JSON
     */
    void unify(RecipeContext context, RecipeJson recipe);
}

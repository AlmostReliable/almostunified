package com.almostreliable.unified.api.recipe;

/**
 * The most basic {@link RecipeUnifier} implementation.
 * <p>
 * This {@link RecipeUnifier} will only be used if no other {@link RecipeUnifier} is registered for a recipe and more
 * specific {@link RecipeUnifier}s such as the {@link ShapedRecipeUnifier} or {@link SmithingRecipeUnifier} can not be
 * applied.<br>
 * It targets the most basic and commonly used keys and structures for inputs and outputs.
 * <p>
 * Custom {@link RecipeUnifier}s can call {@link GenericRecipeUnifier#unify(UnificationHelper, RecipeJson)} on the
 * {@link GenericRecipeUnifier#INSTANCE} to apply the defaults.
 * <p>
 * For more specific {@link RecipeUnifier} implementations, see {@link ShapedRecipeUnifier} and
 * {@link SmithingRecipeUnifier}.
 */
public final class GenericRecipeUnifier implements RecipeUnifier {

    public static final GenericRecipeUnifier INSTANCE = new GenericRecipeUnifier();

    @Override
    public void unify(UnificationHelper helper, RecipeJson recipe) {
        unifyInputs(helper, recipe);
        unifyOutputs(helper, recipe);
    }

    public void unifyInputs(UnificationHelper helper, RecipeJson recipe) {
        helper.unifyInputs(recipe, RecipeConstants.DEFAULT_INPUT_KEYS);
    }

    public void unifyOutputs(UnificationHelper helper, RecipeJson recipe) {
        helper.unifyOutputs(recipe, RecipeConstants.DEFAULT_OUTPUT_KEYS);
    }
}

package com.almostreliable.unified.api.unification.bundled;

import net.minecraft.resources.ResourceLocation;

import com.almostreliable.unified.api.constant.RecipeConstants;
import com.almostreliable.unified.api.unification.recipe.RecipeData;
import com.almostreliable.unified.api.unification.recipe.RecipeJson;
import com.almostreliable.unified.api.unification.recipe.RecipeUnifier;
import com.almostreliable.unified.api.unification.recipe.UnificationHelper;

/**
 * The {@link RecipeUnifier} implementation for smithing recipes.
 * <p>
 * This {@link RecipeUnifier} will only be used if no other {@link RecipeUnifier} is registered for a recipe. It targets
 * vanilla smithing recipes and custom recipe types that use common properties of smithing recipes.<br>
 * If this {@link RecipeUnifier} can't be applied for a recipe, the {@link GenericRecipeUnifier} will be used as the
 * last fallback.
 * <p>
 * To check if a recipe is applicable for this {@link RecipeUnifier}, use
 * {@link SmithingRecipeUnifier#isApplicable(RecipeData)}. Custom {@link RecipeUnifier}s can call
 * {@link SmithingRecipeUnifier#unify(UnificationHelper, RecipeJson)} on the {@link SmithingRecipeUnifier#INSTANCE} to
 * apply the defaults.
 *
 * @since 1.0.0
 */
public final class SmithingRecipeUnifier implements RecipeUnifier {

    public static final SmithingRecipeUnifier INSTANCE = new SmithingRecipeUnifier();
    public static final ResourceLocation TRANSFORM_TYPE = ResourceLocation.withDefaultNamespace("smithing_transform");
    public static final ResourceLocation TRIM_TYPE = ResourceLocation.withDefaultNamespace("smithing_trim");
    public static final String ADDITION_PROPERTY = "addition";
    public static final String BASE_PROPERTY = "base";
    public static final String TEMPLATE_PROPERTY = "template";

    @Override
    public void unify(UnificationHelper helper, RecipeJson recipe) {
        GenericRecipeUnifier.INSTANCE.unify(helper, recipe);
        helper.unifyInputs(recipe, ADDITION_PROPERTY, BASE_PROPERTY, TEMPLATE_PROPERTY);
    }

    /**
     * Checks if this {@link RecipeUnifier} can be applied for the given {@link RecipeData}.
     * <p>
     * This method checks for the vanilla smithing recipe type. If it's a custom recipe type, it tries to find common
     * keys for the smithing recipe.
     *
     * @param recipe the recipe to check
     * @return true if the {@link RecipeUnifier} can be applied, false otherwise
     */
    public static boolean isApplicable(RecipeData recipe) {
        return recipe.getType().equals(TRANSFORM_TYPE) ||
               recipe.getType().equals(TRIM_TYPE) ||
               hasSmithingLikeStructure(recipe);
    }

    @SuppressWarnings("FoldExpressionIntoStream")
    private static boolean hasSmithingLikeStructure(RecipeData recipe) {
        return recipe.hasProperty(ADDITION_PROPERTY) &&
               recipe.hasProperty(BASE_PROPERTY) &&
               recipe.hasProperty(RecipeConstants.RESULT);
    }
}

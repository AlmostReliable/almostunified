package com.almostreliable.unified.api.unification.bundled;

import com.almostreliable.unified.api.constant.RecipeConstants;
import com.almostreliable.unified.api.unification.recipe.RecipeData;
import com.almostreliable.unified.api.unification.recipe.RecipeJson;
import com.almostreliable.unified.api.unification.recipe.RecipeUnifier;
import com.almostreliable.unified.api.unification.recipe.UnificationHelper;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

/**
 * The {@link RecipeUnifier} implementation for shaped crafting recipes.
 * <p>
 * This {@link RecipeUnifier} will only be used if no other {@link RecipeUnifier} is registered for a recipe. It targets
 * vanilla shaped crafting recipes and custom recipe types that use common properties of shaped crafting recipes.<br>
 * If this {@link RecipeUnifier} can't be applied for a recipe, the {@link GenericRecipeUnifier} will be used as the
 * last fallback.
 * <p>
 * To check if a recipe is applicable for this {@link RecipeUnifier}, use
 * {@link ShapedRecipeUnifier#isApplicable(RecipeData)}. Custom {@link RecipeUnifier}s can call
 * {@link ShapedRecipeUnifier#unify(UnificationHelper, RecipeJson)} on the {@link ShapedRecipeUnifier#INSTANCE} to apply
 * the defaults.
 *
 * @since 1.0.0
 */
public final class ShapedRecipeUnifier implements RecipeUnifier {

    public static final RecipeUnifier INSTANCE = new ShapedRecipeUnifier();
    public static final ResourceLocation SHAPED_TYPE = ResourceLocation.withDefaultNamespace("crafting_shaped");
    public static final String KEY_PROPERTY = "key";
    public static final String PATTERN_PROPERTY = "pattern";

    @Override
    public void unify(UnificationHelper helper, RecipeJson recipe) {
        GenericRecipeUnifier.INSTANCE.unify(helper, recipe);

        if (recipe.getProperty(KEY_PROPERTY) instanceof JsonObject json) {
            for (var e : json.entrySet()) {
                helper.unifyInputElement(e.getValue());
            }
        }
    }

    /**
     * Checks if this {@link RecipeUnifier} can be applied for the given {@link RecipeData}.
     * <p>
     * This method checks for the vanilla shaped crafting recipe type. If it's a custom recipe type, it tries to find
     * common keys for the shaped crafting recipe.
     *
     * @param recipe the recipe to check
     * @return true if the {@link RecipeUnifier} can be applied, false otherwise
     */
    public static boolean isApplicable(RecipeData recipe) {
        return recipe.getType().equals(SHAPED_TYPE) || hasShapedCraftingLikeStructure(recipe);
    }

    @SuppressWarnings("FoldExpressionIntoStream")
    private static boolean hasShapedCraftingLikeStructure(RecipeData recipe) {
        return recipe.hasProperty(KEY_PROPERTY) &&
               recipe.hasProperty(PATTERN_PROPERTY) &&
               recipe.hasProperty(RecipeConstants.RESULT);
    }
}

package com.almostreliable.unified.recipe.unifier;

import com.almostreliable.unified.api.recipe.*;
import net.minecraft.resources.ResourceLocation;

public class SmithingRecipeUnifier implements RecipeUnifier {

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

    public boolean matches(RecipeData recipe) {
        return recipe.getType().equals(TRANSFORM_TYPE) ||
               recipe.getType().equals(TRIM_TYPE) ||
               tryMatchModdedRecipe(recipe);
    }

    /**
     * Some mods having fun registering their own smithing types, so we try to catch them all
     *
     * @param recipe the recipe data
     * @return true if the recipe matches
     */
    private boolean tryMatchModdedRecipe(RecipeData recipe) {
        return recipe.hasProperty(SmithingRecipeUnifier.ADDITION_PROPERTY) &&
               recipe.hasProperty(SmithingRecipeUnifier.BASE_PROPERTY) &&
               recipe.hasProperty(RecipeConstants.RESULT);
    }
}

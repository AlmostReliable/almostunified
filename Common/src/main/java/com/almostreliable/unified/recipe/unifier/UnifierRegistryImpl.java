package com.almostreliable.unified.recipe.unifier;

import com.almostreliable.unified.api.UnifierRegistry;
import com.almostreliable.unified.api.recipe.RecipeData;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class UnifierRegistryImpl implements UnifierRegistry {

    private final Map<ResourceLocation, RecipeUnifier> unifiersByType = new HashMap<>();
    private final Map<String, RecipeUnifier> unifiersByModId = new HashMap<>();

    @Override
    public void registerForRecipeType(ResourceLocation recipeType, RecipeUnifier unifier) {
        unifiersByType.put(recipeType, unifier);
    }

    @Override
    public void registerForModId(String modId, RecipeUnifier unifier) {
        unifiersByModId.put(modId, unifier);
    }

    @Override
    public RecipeUnifier getUnifier(RecipeData recipeData) {
        // TODO move the type and modid thing into plugin
        var type = recipeData.getType();
        var byType = unifiersByType.get(type);
        if (byType != null) {
            return byType;
        }

        var byMod = unifiersByModId.get(type.getNamespace());
        if (byMod != null) {
            return byMod;
        }

        if (SmithingRecipeUnifier.INSTANCE.matches(recipeData)) {
            return SmithingRecipeUnifier.INSTANCE;
        }

        if (recipeData.hasProperty(ShapedRecipeUnifier.PATTERN_PROPERTY) &&
            recipeData.hasProperty(ShapedRecipeUnifier.KEY_PROPERTY)) {
            return ShapedRecipeUnifier.INSTANCE;
        }

        return GenericRecipeUnifier.INSTANCE;
    }
}

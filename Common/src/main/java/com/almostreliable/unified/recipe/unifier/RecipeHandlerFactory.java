package com.almostreliable.unified.recipe.unifier;

import com.almostreliable.unified.api.recipe.RecipeConstants;
import com.almostreliable.unified.api.recipe.RecipeData;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.RecipeUnifierBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class RecipeHandlerFactory {

    private final Map<ResourceLocation, RecipeUnifier> transformersByType = new HashMap<>();
    private final Map<String, RecipeUnifier> transformersByModId = new HashMap<>();

    public RecipeUnifier getUnifier(RecipeData recipeData) {
        // TODO move the type and modid thing into plugin
        var type = recipeData.getType();
        var byType = transformersByType.get(type);
        if (byType != null) {
            return byType;
        }

        var byMod = transformersByModId.get(type.getNamespace());
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

    public void registerForType(ResourceLocation type, RecipeUnifier transformer) {
        transformersByType.put(type, transformer);
    }

    public void registerForMod(String mod, RecipeUnifier transformer) {
        transformersByModId.put(mod, transformer);
    }
}

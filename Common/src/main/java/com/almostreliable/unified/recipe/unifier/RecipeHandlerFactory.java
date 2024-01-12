package com.almostreliable.unified.recipe.unifier;

import com.almostreliable.unified.api.recipe.*;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class RecipeHandlerFactory {

    private static final ResourceLocation SMITHING_TYPE = new ResourceLocation("minecraft:smithing");

    private final Map<ResourceLocation, RecipeUnifier> transformersByType = new HashMap<>();
    private final Map<String, RecipeUnifier> transformersByModId = new HashMap<>();

    public void fillUnifier(RecipeUnifierBuilder builder, RecipeData recipeData) {
        GenericRecipeUnifier.INSTANCE.collectUnifier(builder);

        if (recipeData.hasProperty(ShapedRecipeKeyUnifier.PATTERN_PROPERTY) &&
            recipeData.hasProperty(ShapedRecipeKeyUnifier.KEY_PROPERTY)) {
            ShapedRecipeKeyUnifier.INSTANCE.collectUnifier(builder);
        }

        if (recipeData.hasProperty(SmithingRecipeUnifier.ADDITION_PROPERTY) &&
            recipeData.hasProperty(SmithingRecipeUnifier.BASE_PROPERTY) &&
            recipeData.hasProperty(RecipeConstants.RESULT)) {
            SmithingRecipeUnifier.INSTANCE.collectUnifier(builder);
        }

        ResourceLocation type = recipeData.getType();
        RecipeUnifier byMod = transformersByModId.get(type.getNamespace());
        if (byMod != null) {
            byMod.collectUnifier(builder);
        }

        RecipeUnifier byType = transformersByType.get(type);
        if (byType != null) {
            byType.collectUnifier(builder);
        }
    }

    public void registerForType(ResourceLocation type, RecipeUnifier transformer) {
        transformersByType.put(type, transformer);
    }

    public void registerForMod(String mod, RecipeUnifier transformer) {
        transformersByModId.put(mod, transformer);
    }
}

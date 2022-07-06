package com.almostreliable.unified.recipe.handler;

import com.almostreliable.unified.api.recipe.RecipeContext;
import com.almostreliable.unified.api.recipe.RecipeHandler;
import com.almostreliable.unified.api.recipe.RecipeTransformationBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class RecipeHandlerFactory {
    private final Map<ResourceLocation, RecipeHandler> transformersByType = new HashMap<>();
    private final Map<String, RecipeHandler> transformersByModId = new HashMap<>();

    public void fillTransformations(RecipeTransformationBuilder builder, RecipeContext context) {
        GenericRecipeHandler.INSTANCE.collectTransformations(builder);

        if (context.hasProperty(ShapedRecipeKeyHandler.PATTERN_PROPERTY) &&
            context.hasProperty(ShapedRecipeKeyHandler.KEY_PROPERTY)) {
            ShapedRecipeKeyHandler.INSTANCE.collectTransformations(builder);
        }

        ResourceLocation type = context.getType();
        RecipeHandler byMod = transformersByModId.get(type.getNamespace());
        if (byMod != null) {
            byMod.collectTransformations(builder);
        }

        RecipeHandler byType = transformersByType.get(type);
        if (byType != null) {
            byType.collectTransformations(builder);
        }
    }

    public void registerForType(ResourceLocation type, RecipeHandler transformer) {
        transformersByType.put(type, transformer);
    }

    public void registerForMod(String mod, RecipeHandler transformer) {
        transformersByModId.put(mod, transformer);
    }
}

package com.almostreliable.unified.recipe.handler;

import com.almostreliable.unified.api.recipe.RecipeContext;
import com.almostreliable.unified.api.recipe.RecipeHandler;
import com.almostreliable.unified.api.recipe.RecipeTransformations;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class RecipeHandlerFactory {
    private final Map<ResourceLocation, RecipeHandler> transformersByType = new HashMap<>();
    private final Map<String, RecipeHandler> transformersByModId = new HashMap<>();

    public void create(RecipeTransformations builder, RecipeContext context) {
        GenericRecipeHandler.INSTANCE.collectTransformations(builder);

        if (context.hasProperty(ShapedRecipeKeyHandler.PATTERN_PROPERTY) &&
            context.hasProperty(ShapedRecipeKeyHandler.KEY_PROPERTY)) {
            ShapedRecipeKeyHandler.INSTANCE.collectTransformations(builder);
        }

        RecipeHandler byMod = transformersByModId.get(context.getModId());
        if (byMod != null) {
            byMod.collectTransformations(builder);
        }

        RecipeHandler byType = transformersByType.get(context.getType());
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

package com.almostreliable.unified.handler;

import com.almostreliable.unified.api.RecipeContext;
import com.almostreliable.unified.api.RecipeHandler;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class RecipeHandlerFactory {
    private final Map<ResourceLocation, RecipeHandler> transformersByType = new HashMap<>();

    @Nullable
    public RecipeHandler create(RecipeContext context) {
        RecipeHandler transformer = transformersByType.get(context.getType());
        if (transformer != null) {
            return transformer;
        }

        if (context.hasProperty(ShapedRecipeKeyHandler.PATTERN_PROPERTY) &&
            context.hasProperty(ShapedRecipeKeyHandler.KEY_PROPERTY)) {
            return ShapedRecipeKeyHandler.INSTANCE;
        }

        if (GenericRecipeHandler.INSTANCE.hasInputOrOutputProperty(context)) {
            return GenericRecipeHandler.INSTANCE;
        }

        return null;
    }

    public void register(ResourceLocation type, RecipeHandler transformer) {
        transformersByType.put(type, transformer);
    }
}

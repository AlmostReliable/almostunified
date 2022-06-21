package com.almostreliable.unified.handler;

import com.almostreliable.unified.api.RecipeContext;
import com.almostreliable.unified.api.RecipeHandler;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class RecipeHandlerFactory {
    private final Map<ResourceLocation, RecipeHandler> transformersByType = new HashMap<>();
    private final Map<String, RecipeHandler> transformersByModId = new HashMap<>();

    @Nullable
    public RecipeHandler create(RecipeContext context) {
        RecipeHandler byType = transformersByType.get(context.getType());
        if (byType != null) {
            return byType;
        }

        RecipeHandler byMod = transformersByModId.get(context.getModId());
        if (byMod != null) {
            return byMod;
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

    public void registerForType(String type, RecipeHandler transformer) {
        transformersByType.put(new ResourceLocation(type), transformer);
    }

    public void registerForType(ResourceLocation type, RecipeHandler transformer) {
        transformersByType.put(type, transformer);
    }

    public void registerForMod(String mod, RecipeHandler transformer) {
        transformersByModId.put(mod, transformer);
    }
}

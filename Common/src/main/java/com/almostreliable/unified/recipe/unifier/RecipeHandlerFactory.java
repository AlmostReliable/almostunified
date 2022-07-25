package com.almostreliable.unified.recipe.unifier;

import com.almostreliable.unified.api.recipe.RecipeContext;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import com.almostreliable.unified.api.recipe.RecipeUnifierBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class RecipeHandlerFactory {
    private final Map<ResourceLocation, RecipeUnifier> transformersByType = new HashMap<>();
    private final Map<String, RecipeUnifier> transformersByModId = new HashMap<>();

    public void fillUnifier(RecipeUnifierBuilder builder, RecipeContext context) {
        GenericRecipeUnifier.INSTANCE.collectUnifier(builder);

        if (context.hasProperty(ShapedRecipeKeyUnifier.PATTERN_PROPERTY) &&
            context.hasProperty(ShapedRecipeKeyUnifier.KEY_PROPERTY)) {
            ShapedRecipeKeyUnifier.INSTANCE.collectUnifier(builder);
        }

        ResourceLocation type = context.getType();
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

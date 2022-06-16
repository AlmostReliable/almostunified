package com.almostreliable.unified.transformer;

import com.almostreliable.unified.api.RecipeTransformer;
import com.almostreliable.unified.api.RecipeTransformerFactory;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public class GenericRecipeTransformerFactory implements RecipeTransformerFactory {

    private final GenericRecipeTransformer transformer = new GenericRecipeTransformer();
    private final Set<String> inputKeys = Set.of("input", "ingredient", "ingredients");
    private final Set<String> outputKeys = Set.of("output", "result", "results");

    @Override
    public RecipeTransformer create(ResourceLocation type, String property) {
        if (inputKeys.contains(property) || outputKeys.contains(property)) {
            return transformer;
        }

        return null;
    }
}

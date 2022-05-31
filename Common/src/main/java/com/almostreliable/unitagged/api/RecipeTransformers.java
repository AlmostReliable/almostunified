package com.almostreliable.unitagged.api;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RecipeTransformers {
    private static final Map<ResourceLocation, RecipeTransformer> TRANSFORMERS = new HashMap<>();

    public static void register(ResourceLocation type, RecipeTransformer transformer) {
        Objects.requireNonNull(type, "type cannot be null");
        Objects.requireNonNull(transformer, "transformer cannot be null");
        TRANSFORMERS.put(type, transformer);
    }

    @Nullable
    public static RecipeTransformer get(ResourceLocation type) {
        return TRANSFORMERS.get(type);
    }

    public static RecipeTransformer getOrDefault(ResourceLocation type, RecipeTransformer defaultTransformer) {
        return TRANSFORMERS.getOrDefault(type, defaultTransformer);
    }
}

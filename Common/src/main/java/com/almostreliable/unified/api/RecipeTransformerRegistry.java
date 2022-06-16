package com.almostreliable.unified.api;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.api.RecipeTransformerFactory;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RecipeTransformerRegistry {
    private static final Map<ResourceLocation, RecipeTransformerFactory> FACTORIES = new HashMap<>();

    public static void registerFactory(ResourceLocation type, RecipeTransformerFactory factory) {
        Objects.requireNonNull(type, "type cannot be null");
        Objects.requireNonNull(factory, "factory cannot be null");

        if (FACTORIES.containsKey(type)) {
            AlmostUnified.LOG.warn("Overwriting transformer factory for {} with {}",
                    type,
                    factory.getClass().getName());
        }

        FACTORIES.put(type, factory);
    }

    @Nullable
    public static RecipeTransformerFactory getFactory(ResourceLocation type) {
        return FACTORIES.get(type);
    }

    public static RecipeTransformerFactory getOrDefault(ResourceLocation type, RecipeTransformerFactory defaultFactory) {
        return FACTORIES.getOrDefault(type, defaultFactory);
    }
}

package com.almostreliable.unified.unification.recipe;

import net.minecraft.resources.ResourceLocation;

import com.almostreliable.unified.api.unification.recipe.CustomIngredientUnifier;
import com.almostreliable.unified.api.unification.recipe.CustomIngredientUnifierRegistry;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CustomIngredientUnifierRegistryImpl implements CustomIngredientUnifierRegistry {

    private final Map<ResourceLocation, CustomIngredientUnifier> ingredientUnifiersByType = new HashMap<>();

    @Override
    public void registerForType(ResourceLocation type, CustomIngredientUnifier customIngredientUnifier) {
        ingredientUnifiersByType.put(type, customIngredientUnifier);
    }

    @Nullable
    @Override
    public CustomIngredientUnifier getCustomIngredientUnifier(ResourceLocation type) {
        return ingredientUnifiersByType.get(type);
    }
}

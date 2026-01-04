package com.almostreliable.unified.unification.recipe;

import net.minecraft.resources.Identifier;

import com.almostreliable.unified.api.unification.recipe.CustomIngredientUnifier;
import com.almostreliable.unified.api.unification.recipe.CustomIngredientUnifierRegistry;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CustomIngredientUnifierRegistryImpl implements CustomIngredientUnifierRegistry {

    private final Map<Identifier, CustomIngredientUnifier> ingredientUnifiersByType = new HashMap<>();

    @Override
    public void registerForType(Identifier type, CustomIngredientUnifier customIngredientUnifier) {
        ingredientUnifiersByType.put(type, customIngredientUnifier);
    }

    @Nullable
    @Override
    public CustomIngredientUnifier getCustomIngredientUnifier(Identifier type) {
        return ingredientUnifiersByType.get(type);
    }
}

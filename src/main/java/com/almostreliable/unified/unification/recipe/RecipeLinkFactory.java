package com.almostreliable.unified.unification.recipe;

import net.minecraft.resources.Identifier;

import com.google.gson.JsonObject;

import javax.annotation.Nullable;

public interface RecipeLinkFactory {

    @Nullable
    RecipeLink create(Identifier id, JsonObject originalRecipe);
}

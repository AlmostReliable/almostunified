package com.almostreliable.unified.unification.recipe;

import net.minecraft.resources.ResourceLocation;

import com.google.gson.JsonObject;

import javax.annotation.Nullable;

public interface RecipeLinkFactory {

    @Nullable
    RecipeLink create(ResourceLocation id, JsonObject recipe);
}

package com.almostreliable.unified.api.recipe;

import net.minecraft.resources.ResourceLocation;

public interface RecipeData {

    ResourceLocation getId();

    ResourceLocation getType();

    boolean hasProperty(String key);
}

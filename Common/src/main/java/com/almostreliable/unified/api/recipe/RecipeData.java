package com.almostreliable.unified.api.recipe;

import net.minecraft.resources.ResourceLocation;

public interface RecipeData {

    ResourceLocation getType();

    boolean hasProperty(String property);

    default String getModId() {
        return getType().getNamespace();
    }

}

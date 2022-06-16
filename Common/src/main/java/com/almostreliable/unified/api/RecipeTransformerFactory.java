package com.almostreliable.unified.api;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;

public interface RecipeTransformerFactory {
    @Nullable
    RecipeTransformer create(ResourceLocation type, String property);
}

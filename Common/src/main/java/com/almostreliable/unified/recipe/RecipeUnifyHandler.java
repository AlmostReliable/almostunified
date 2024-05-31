package com.almostreliable.unified.recipe;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public interface RecipeUnifyHandler {

    void run(Map<ResourceLocation, JsonElement> recipes, boolean skipClientTracking);
}

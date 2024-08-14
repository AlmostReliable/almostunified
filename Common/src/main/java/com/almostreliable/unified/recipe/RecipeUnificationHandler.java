package com.almostreliable.unified.recipe;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public interface RecipeUnificationHandler {

    void run(Map<ResourceLocation, JsonElement> recipes);
}

package com.almostreliable.unified;

import com.almostreliable.unified.recipe.ClientRecipeTracker;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class AlmostUnifiedFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        if (!AlmostUnified.getStartupConfig().isServerOnly()) {
            Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, ClientRecipeTracker.ID, ClientRecipeTracker.SERIALIZER);
            Registry.register(BuiltInRegistries.RECIPE_TYPE, ClientRecipeTracker.ID, ClientRecipeTracker.TYPE);
        }
    }
}

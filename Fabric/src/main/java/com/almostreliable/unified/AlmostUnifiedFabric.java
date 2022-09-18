package com.almostreliable.unified;

import com.almostreliable.unified.recipe.ClientRecipeTracker;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;

public class AlmostUnifiedFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Registry.register(Registry.RECIPE_SERIALIZER, ClientRecipeTracker.ID, ClientRecipeTracker.SERIALIZER);
        Registry.register(Registry.RECIPE_TYPE, ClientRecipeTracker.ID, ClientRecipeTracker.TYPE);
    }
}

package com.almostreliable.unified.core;


import com.almostreliable.unified.api.constant.ModConstants;
import com.almostreliable.unified.api.plugin.AlmostUnifiedPlugin;
import com.almostreliable.unified.api.unification.recipe.RecipeUnifierRegistry;
import com.almostreliable.unified.compat.unification.AmethystImbuementRecipeUnifier;
import com.almostreliable.unified.utils.Utils;
import net.minecraft.resources.ResourceLocation;

public class FabricPlugin implements AlmostUnifiedPlugin {

    @Override
    public ResourceLocation getPluginId() {
        return Utils.getRL("fabric");
    }

    @Override
    public void registerRecipeUnifiers(RecipeUnifierRegistry registry) {
        registry.registerForModId(ModConstants.AMETHYST_IMBUEMENT, new AmethystImbuementRecipeUnifier());
    }
}

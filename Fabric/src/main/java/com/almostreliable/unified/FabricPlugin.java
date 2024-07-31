package com.almostreliable.unified;


import com.almostreliable.unified.api.ModConstants;
import com.almostreliable.unified.api.RecipeUnifierRegistry;
import com.almostreliable.unified.api.plugin.AlmostUnifiedPlugin;
import com.almostreliable.unified.compat.AmethystImbuementRecipeUnifier;
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

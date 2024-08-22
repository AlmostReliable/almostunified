package com.almostreliable.unified.core;

import com.almostreliable.unified.api.constant.ModConstants;
import com.almostreliable.unified.api.plugin.AlmostUnifiedNeoPlugin;
import com.almostreliable.unified.api.plugin.AlmostUnifiedPlugin;
import com.almostreliable.unified.api.unification.recipe.RecipeUnifierRegistry;
import com.almostreliable.unified.compat.unification.GregTechModernRecipeUnifier;
import com.almostreliable.unified.utils.Utils;
import net.minecraft.resources.ResourceLocation;

@AlmostUnifiedNeoPlugin
public class CommonPlugin implements AlmostUnifiedPlugin {

    @Override
    public ResourceLocation getPluginId() {
        return Utils.getRL("common");
    }

    @Override
    public void registerRecipeUnifiers(RecipeUnifierRegistry registry) {
        registry.registerForModId(ModConstants.GREGTECH_MODERN, new GregTechModernRecipeUnifier());
    }
}

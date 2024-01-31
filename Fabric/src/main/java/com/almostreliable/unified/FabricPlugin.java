package com.almostreliable.unified;


import com.almostreliable.unified.api.ModConstants;
import com.almostreliable.unified.api.UnifierRegistry;
import com.almostreliable.unified.api.plugin.AlmostUnifiedPlugin;
import com.almostreliable.unified.compat.AmethystImbuementRecipeUnifier;
import com.almostreliable.unified.compat.ModernIndustrializationRecipeUnifier;
import net.minecraft.resources.ResourceLocation;

public class FabricPlugin implements AlmostUnifiedPlugin {
    @Override
    public ResourceLocation getPluginId() {
        return new ResourceLocation(BuildConfig.MOD_ID, "fabric");
    }

    @Override
    public void registerUnifiers(UnifierRegistry registry) {
        registry.registerForMod(ModConstants.AMETHYST_IMBUEMENT, new AmethystImbuementRecipeUnifier());
        registry.registerForMod(ModConstants.MODERN_INDUSTRIALIZATION, new ModernIndustrializationRecipeUnifier());
    }
}

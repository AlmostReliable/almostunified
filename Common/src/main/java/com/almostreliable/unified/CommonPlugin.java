package com.almostreliable.unified;

import com.almostreliable.unified.api.ModConstants;
import com.almostreliable.unified.api.UnifierRegistry;
import com.almostreliable.unified.api.plugin.AlmostUnifiedNeoPlugin;
import com.almostreliable.unified.api.plugin.AlmostUnifiedPlugin;
import com.almostreliable.unified.compat.AdAstraRecipeUnifier;
import com.almostreliable.unified.compat.GregTechModernRecipeUnifier;
import com.almostreliable.unified.utils.Utils;
import net.minecraft.resources.ResourceLocation;

@AlmostUnifiedNeoPlugin
public class CommonPlugin implements AlmostUnifiedPlugin {
    @Override
    public ResourceLocation getPluginId() {
        return Utils.getRL("common");
    }

    @Override
    public void registerUnifiers(UnifierRegistry registry) {
        registry.registerForMod(ModConstants.AD_ASTRA, new AdAstraRecipeUnifier());
        registry.registerForMod(ModConstants.GREGTECH_MODERN, new GregTechModernRecipeUnifier());
    }
}

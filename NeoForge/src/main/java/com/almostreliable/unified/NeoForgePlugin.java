package com.almostreliable.unified;

import com.almostreliable.unified.api.ModConstants;
import com.almostreliable.unified.api.UnifierRegistry;
import com.almostreliable.unified.api.plugin.AlmostUnifiedNeoPlugin;
import com.almostreliable.unified.api.plugin.AlmostUnifiedPlugin;
import com.almostreliable.unified.compat.*;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

@AlmostUnifiedNeoPlugin
public class NeoForgePlugin implements AlmostUnifiedPlugin {

    @Override
    public ResourceLocation getPluginId() {
        return new ResourceLocation(BuildConfig.MOD_ID, "neoforge");
    }

    @Override
    public void registerUnifiers(UnifierRegistry registry) {
        List.of(
                ModConstants.ARS_CREO,
                ModConstants.ARS_ELEMENTAL,
                ModConstants.ARS_NOUVEAU,
                ModConstants.ARS_SCALAES
        ).forEach(modId -> registry.registerForMod(modId, new ArsNouveauRecipeUnifier()));
        registry.registerForMod(ModConstants.CYCLIC, new CyclicRecipeUnifier());
        registry.registerForMod(ModConstants.ENDER_IO, new EnderIORecipeUnifier());
        registry.registerForMod(ModConstants.IMMERSIVE_ENGINEERING, new ImmersiveEngineeringRecipeUnifier());
        registry.registerForMod(ModConstants.INTEGRATED_DYNAMICS, new IntegratedDynamicsRecipeUnifier());
        registry.registerForMod(ModConstants.MEKANISM, new MekanismRecipeUnifier());
    }
}

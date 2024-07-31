package com.almostreliable.unified;

import com.almostreliable.unified.api.ModConstants;
import com.almostreliable.unified.api.RecipeUnifierRegistry;
import com.almostreliable.unified.api.plugin.AlmostUnifiedNeoPlugin;
import com.almostreliable.unified.api.plugin.AlmostUnifiedPlugin;
import com.almostreliable.unified.compat.*;
import com.almostreliable.unified.recipe.unifier.ShapedRecipeUnifier;
import com.almostreliable.unified.utils.Utils;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

@AlmostUnifiedNeoPlugin
public class NeoForgePlugin implements AlmostUnifiedPlugin {

    @Override
    public ResourceLocation getPluginId() {
        return Utils.getRL("neoforge");
    }

    @Override
    public void registerRecipeUnifiers(RecipeUnifierRegistry registry) {
        List.of(
                ModConstants.ARS_CREO,
                ModConstants.ARS_ELEMENTAL,
                ModConstants.ARS_NOUVEAU,
                ModConstants.ARS_SCALAES
        ).forEach(modId -> registry.registerForModId(modId, new ArsNouveauRecipeUnifier()));
        registry.registerForModId(ModConstants.CYCLIC, new CyclicRecipeUnifier());
        registry.registerForModId(ModConstants.ENDER_IO, new EnderIORecipeUnifier());
        registry.registerForModId(ModConstants.IMMERSIVE_ENGINEERING, new ImmersiveEngineeringRecipeUnifier());
        registry.registerForModId(ModConstants.INTEGRATED_DYNAMICS, new IntegratedDynamicsRecipeUnifier());
        registry.registerForModId(ModConstants.MEKANISM, new MekanismRecipeUnifier());
        registry.registerForModId(ModConstants.MODERN_INDUSTRIALIZATION, new ModernIndustrializationRecipeUnifier());
        registry.registerForModId(ModConstants.OCCULTISM, new OccultismRecipeUnifier());
        registry.registerForModId(ModConstants.PRODUCTIVE_TREES, new ProductiveTreesRecipeUnifier());
        registry.registerForModId(ModConstants.THEURGY, new TheurgyRecipeUnifier());
        registry.registerForRecipeType(
                ResourceLocation.fromNamespaceAndPath(ModConstants.THEURGY, "divination_rod"),
                ShapedRecipeUnifier.INSTANCE
        );
    }
}

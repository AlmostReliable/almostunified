package com.almostreliable.unified.core;

import net.minecraft.resources.ResourceLocation;

import com.almostreliable.unified.api.constant.ModConstants;
import com.almostreliable.unified.api.plugin.AlmostUnifiedNeoPlugin;
import com.almostreliable.unified.api.plugin.AlmostUnifiedPlugin;
import com.almostreliable.unified.api.unification.bundled.ShapedRecipeUnifier;
import com.almostreliable.unified.api.unification.recipe.RecipeUnifierRegistry;
import com.almostreliable.unified.compat.unification.ArsNouveauRecipeUnifier;
import com.almostreliable.unified.compat.unification.CyclicRecipeUnifier;
import com.almostreliable.unified.compat.unification.EnderIORecipeUnifier;
import com.almostreliable.unified.compat.unification.ImmersiveEngineeringRecipeUnifier;
import com.almostreliable.unified.compat.unification.IntegratedDynamicsRecipeUnifier;
import com.almostreliable.unified.compat.unification.MekanismRecipeUnifier;
import com.almostreliable.unified.compat.unification.ModernIndustrializationRecipeUnifier;
import com.almostreliable.unified.compat.unification.OccultismRecipeUnifier;
import com.almostreliable.unified.compat.unification.ProductiveTreesRecipeUnifier;
import com.almostreliable.unified.compat.unification.TheurgyRecipeUnifier;
import com.almostreliable.unified.utils.Utils;

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
        registry.registerForModId(ModConstants.EXTENDED_INDUSTRIALIZATION, new ModernIndustrializationRecipeUnifier());
        registry.registerForModId(ModConstants.OCCULTISM, new OccultismRecipeUnifier());
        registry.registerForModId(ModConstants.PRODUCTIVE_TREES, new ProductiveTreesRecipeUnifier());
        registry.registerForModId(ModConstants.THEURGY, new TheurgyRecipeUnifier());
        registry.registerForRecipeType(
            ResourceLocation.fromNamespaceAndPath(ModConstants.THEURGY, "divination_rod"),
            ShapedRecipeUnifier.INSTANCE
        );
    }
}

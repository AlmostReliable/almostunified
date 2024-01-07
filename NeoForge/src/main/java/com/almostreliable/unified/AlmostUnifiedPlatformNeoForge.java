package com.almostreliable.unified;

import com.almostreliable.unified.api.ModConstants;
import com.almostreliable.unified.compat.*;
import com.almostreliable.unified.recipe.unifier.RecipeHandlerFactory;
import com.almostreliable.unified.utils.UnifyTag;
import com.google.auto.service.AutoService;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModInfo;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@AutoService(AlmostUnifiedPlatform.class)
public class AlmostUnifiedPlatformNeoForge implements AlmostUnifiedPlatform {

    @Override
    public Platform getPlatform() {
        return Platform.FORGE;
    }

    @Override
    public boolean isModLoaded(String modId) {
        if (ModList.get() == null) {
            return LoadingModList.get().getMods().stream().map(ModInfo::getModId).anyMatch(modId::equals);
        }
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isClient() {
        return FMLLoader.getDist() == Dist.CLIENT;
    }

    @Override
    public Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get().resolve(BuildConfig.MOD_ID);
    }

    @Override
    public Path getLogPath() {
        return FMLPaths.GAMEDIR.get().resolve("logs").resolve(BuildConfig.MOD_ID);
    }

    @Override
    public void bindRecipeHandlers(RecipeHandlerFactory factory) {
        factory.registerForMod(ModConstants.AD_ASTRA, new AdAstraRecipeUnifier());
        List.of(
                ModConstants.ARS_CREO,
                ModConstants.ARS_ELEMENTAL,
                ModConstants.ARS_NOUVEAU,
                ModConstants.ARS_SCALAES
        ).forEach(modId -> factory.registerForMod(modId, new ArsNouveauRecipeUnifier()));
        factory.registerForMod(ModConstants.CYCLIC, new CyclicRecipeUnifier());
        factory.registerForMod(ModConstants.ENDER_IO, new EnderIORecipeUnifier());
        factory.registerForMod(ModConstants.GREGTECH_MODERN, new GregTechModernRecipeUnifier());
        factory.registerForMod(ModConstants.IMMERSIVE_ENGINEERING, new ImmersiveEngineeringRecipeUnifier());
        factory.registerForMod(ModConstants.INTEGRATED_DYNAMICS, new IntegratedDynamicsRecipeUnifier());
        factory.registerForMod(ModConstants.MEKANISM, new MekanismRecipeUnifier());
    }

    @Override
    public Set<UnifyTag<Item>> getStoneStrataTags(List<String> stoneStrataIds) {
        return stoneStrataIds
                .stream()
                .map(id -> new ResourceLocation("forge", "ores_in_ground/" + id))
                .map(UnifyTag::item)
                .collect(Collectors.toSet());
    }
}

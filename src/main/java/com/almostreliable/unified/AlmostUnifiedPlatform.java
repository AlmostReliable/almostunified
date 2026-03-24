package com.almostreliable.unified;

import com.almostreliable.unified.api.constant.ModConstants;
import com.almostreliable.unified.core.ConditionalRecipeLinkFactory;
import com.almostreliable.unified.mixin.ContextAwareReloadListenerAccessor;
import com.almostreliable.unified.unification.recipe.RecipeLink;
import com.almostreliable.unified.unification.recipe.RecipeLinkFactory;

import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.moddiscovery.ModInfo;

import java.nio.file.Path;

public interface AlmostUnifiedPlatform {

    AlmostUnifiedPlatform INSTANCE = new AlmostUnifiedPlatform() {};

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    default boolean isModLoaded(String modId) {
        if (ModList.get() == null) {
            return FMLLoader.getCurrent().getLoadingModList().getMods().stream().map(ModInfo::getModId).anyMatch(modId::equals);
        }
        return ModList.get().isLoaded(modId);
    }

    default boolean isClient() {
        return FMLLoader.getCurrent().getDist() == Dist.CLIENT;
    }

    default Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get().resolve(ModConstants.ALMOST_UNIFIED);
    }

    default Path getDebugLogPath() {
        return FMLPaths.GAMEDIR.get().resolve("logs").resolve(ModConstants.ALMOST_UNIFIED).resolve("debug");
    }

    default RecipeLinkFactory getRecipeLinkFactory(RecipeManager recipeManager, boolean cache) {
        try {
            var conOps = ((ContextAwareReloadListenerAccessor) recipeManager).au$makeConditionalOps();
            return new ConditionalRecipeLinkFactory(conOps, cache);
        } catch (Exception e) {
            AlmostUnifiedCommon.LOGGER.error(e.getMessage(), e);
            return RecipeLink::of;
        }
    }
}

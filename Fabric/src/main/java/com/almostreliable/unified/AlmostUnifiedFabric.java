package com.almostreliable.unified;

import com.almostreliable.unified.api.plugin.AlmostUnifiedPlugin;
import com.almostreliable.unified.recipe.ClientRecipeTracker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.ArrayList;
import java.util.List;

public class AlmostUnifiedFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        if (!AlmostUnified.STARTUP_CONFIG.isServerOnly()) {
            Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                    ClientRecipeTracker.ID,
                    ClientRecipeTracker.SERIALIZER);
            Registry.register(BuiltInRegistries.RECIPE_TYPE, ClientRecipeTracker.ID, ClientRecipeTracker.TYPE);
        }

        initializePluginManager();
    }

    private static void initializePluginManager() {
        List<AlmostUnifiedPlugin> plugins = new ArrayList<>();
        for (EntrypointContainer<AlmostUnifiedPlugin> entries : FabricLoader
                .getInstance()
                .getEntrypointContainers(BuildConfig.MOD_ID, AlmostUnifiedPlugin.class)) {
            try {
                plugins.add(entries.getEntrypoint());
            } catch (Exception e) {
                AlmostUnified.LOGGER.error("Failed to create AlmostUnified plugin, while loading it: ", e);
            }
        }

        PluginManager.initialize(plugins);
    }
}

package com.almostreliable.unified;

import com.almostreliable.unified.api.plugin.AlmostUnifiedPlugin;
import com.almostreliable.unified.loot.LootUnification;
import com.almostreliable.unified.recipe.ClientRecipeTracker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class AlmostUnifiedFabric implements ModInitializer {

    public static final ResourceLocation TABLE_EVENT_PHASE = new ResourceLocation(BuildConfig.MOD_ID, "unify_loot");
    @Override
    public void onInitialize() {
        if (!AlmostUnified.getStartupConfig().isServerOnly()) {
            Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                    ClientRecipeTracker.ID,
                    ClientRecipeTracker.SERIALIZER);
            Registry.register(BuiltInRegistries.RECIPE_TYPE, ClientRecipeTracker.ID, ClientRecipeTracker.TYPE);
        }

        initializePluginManager();

        LootTableEvents.ALL_LOADED.register((resourceManager, lootManager) -> LootUnification.unifyLoot(lootManager));
        LootTableEvents.ALL_LOADED.addPhaseOrdering(TABLE_EVENT_PHASE, Event.DEFAULT_PHASE);

    }

    private static void initializePluginManager() {
        List<AlmostUnifiedPlugin> plugins = new ArrayList<>();
        for (EntrypointContainer<AlmostUnifiedPlugin> entries : FabricLoader
                .getInstance()
                .getEntrypointContainers(BuildConfig.MOD_ID, AlmostUnifiedPlugin.class)) {
            try {
                plugins.add(entries.getEntrypoint());
            } catch (Exception e) {
                AlmostUnified.LOG.error("Failed to create AlmostUnified plugin, while loading it: ", e);
            }
        }

        PluginManager.initialize(plugins);
    }
}

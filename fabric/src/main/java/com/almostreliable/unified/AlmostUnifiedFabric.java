package com.almostreliable.unified;

import com.almostreliable.unified.api.constant.ModConstants;
import com.almostreliable.unified.api.plugin.AlmostUnifiedPlugin;
import com.almostreliable.unified.compat.PluginManager;
import com.almostreliable.unified.compat.viewer.ClientRecipeTracker;
import com.almostreliable.unified.core.AlmostUnifiedCommands;
import com.almostreliable.unified.unification.worldgen.WorldStripper;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.ArrayList;
import java.util.List;

public class AlmostUnifiedFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        if (!AlmostUnifiedCommon.STARTUP_CONFIG.isServerOnly()) {
            Registry.register(
                BuiltInRegistries.RECIPE_SERIALIZER,
                ClientRecipeTracker.ID,
                ClientRecipeTracker.SERIALIZER
            );
            Registry.register(BuiltInRegistries.RECIPE_TYPE, ClientRecipeTracker.ID, ClientRecipeTracker.TYPE);
        }

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            AlmostUnifiedCommands.registerCommands(dispatcher));
        ServerTickEvents.END_SERVER_TICK.register(server -> WorldStripper.onServerTick());

        initializePluginManager();
    }

    private static void initializePluginManager() {
        List<AlmostUnifiedPlugin> plugins = new ArrayList<>();
        var entrypointContainers = FabricLoader.getInstance()
            .getEntrypointContainers(ModConstants.ALMOST_UNIFIED, AlmostUnifiedPlugin.class);

        for (var entrypointContainer : entrypointContainers) {
            try {
                plugins.add(entrypointContainer.getEntrypoint());
            } catch (Exception e) {
                AlmostUnifiedCommon.LOGGER.error(
                    "Failed to load plugin for mod {}.",
                    entrypointContainer.getProvider().getMetadata().getName(),
                    e
                );
            }
        }

        PluginManager.init(plugins);
    }
}

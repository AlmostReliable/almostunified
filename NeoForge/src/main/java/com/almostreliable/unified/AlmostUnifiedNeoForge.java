package com.almostreliable.unified;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;

import com.almostreliable.unified.api.constant.ModConstants;
import com.almostreliable.unified.api.plugin.AlmostUnifiedNeoPlugin;
import com.almostreliable.unified.api.plugin.AlmostUnifiedPlugin;
import com.almostreliable.unified.compat.PluginManager;
import com.almostreliable.unified.compat.viewer.ClientRecipeTracker;
import com.almostreliable.unified.core.AlmostUnifiedCommands;
import com.almostreliable.unified.unification.worldgen.WorldGenBiomeModifier;
import com.almostreliable.unified.unification.worldgen.WorldStripper;
import com.almostreliable.unified.utils.Utils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.objectweb.asm.Type;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mod(ModConstants.ALMOST_UNIFIED)
public class AlmostUnifiedNeoForge {

    public AlmostUnifiedNeoForge(IEventBus eventBus) {
        eventBus.addListener(this::onRegisterEvent);
        eventBus.addListener(this::onCommonSetup);
        NeoForge.EVENT_BUS.addListener(this::onCommandsRegister);
        NeoForge.EVENT_BUS.addListener(WorldStripper::onServerTick);
    }

    private void onRegisterEvent(RegisterEvent event) {
        if (event.getRegistryKey() == NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS) {
            Registry.register(
                NeoForgeRegistries.BIOME_MODIFIER_SERIALIZERS,
                Utils.getRL("worldgen_unification"),
                WorldGenBiomeModifier.CODEC
            );
        }

        if (AlmostUnifiedCommon.STARTUP_CONFIG.isServerOnly()) return;

        if (event.getRegistryKey() == Registries.RECIPE_SERIALIZER) {
            Registry.register(
                BuiltInRegistries.RECIPE_SERIALIZER,
                ClientRecipeTracker.ID,
                ClientRecipeTracker.SERIALIZER
            );
        }

        if (event.getRegistryKey() == Registries.RECIPE_TYPE) {
            Registry.register(BuiltInRegistries.RECIPE_TYPE, ClientRecipeTracker.ID, ClientRecipeTracker.TYPE);
        }
    }

    private void onCommandsRegister(RegisterCommandsEvent event) {
        AlmostUnifiedCommands.registerCommands(event.getDispatcher());
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(AlmostUnifiedNeoForge::initializePluginManager);
    }

    private static void initializePluginManager() {
        List<AlmostUnifiedPlugin> plugins = new ArrayList<>();
        Collection<Class<AlmostUnifiedPlugin>> pluginClasses = getPluginClasses();

        for (var pluginClass : pluginClasses) {
            try {
                plugins.add(pluginClass.getConstructor().newInstance());
            } catch (Exception e) {
                AlmostUnifiedCommon.LOGGER.error("Failed to load plugin {}.", pluginClass.getName(), e);
            }
        }

        PluginManager.init(plugins);
    }

    private static Collection<Class<AlmostUnifiedPlugin>> getPluginClasses() {
        Set<Class<AlmostUnifiedPlugin>> pluginClasses = new HashSet<>();
        Type type = Type.getType(AlmostUnifiedNeoPlugin.class);

        for (var data : ModList.get().getAllScanData()) {
            for (var annotation : data.getAnnotations()) {
                if (!annotation.annotationType().equals(type)) {
                    continue;
                }

                var plugin = getPluginClass(annotation.clazz().getClassName());
                if (plugin != null) {
                    pluginClasses.add(plugin);
                }
            }
        }

        return pluginClasses;
    }

    @Nullable
    private static Class<AlmostUnifiedPlugin> getPluginClass(String className) {
        try {
            Class<?> pluginClass = Class.forName(className);
            if (AlmostUnifiedPlugin.class.isAssignableFrom(pluginClass)) {
                // noinspection unchecked
                return (Class<AlmostUnifiedPlugin>) pluginClass;
            }

            AlmostUnifiedCommon.LOGGER.error(
                "Plugin {} does not implement {}.",
                className,
                AlmostUnifiedPlugin.class.getName()
            );
        } catch (ClassNotFoundException e) {
            AlmostUnifiedCommon.LOGGER.error("Failed to load plugin {}.", className, e);
            return null;
        }

        return null;
    }
}

package com.almostreliable.unified;

import com.almostreliable.unified.api.plugin.AlmostUnifiedNeoPlugin;
import com.almostreliable.unified.api.plugin.AlmostUnifiedPlugin;
import com.almostreliable.unified.loot.LootUnification;
import com.almostreliable.unified.recipe.ClientRecipeTracker;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.objectweb.asm.Type;

import javax.annotation.Nullable;
import java.util.*;

@Mod(BuildConfig.MOD_ID)
public class AlmostUnifiedForge {

    public AlmostUnifiedForge(IEventBus eventBus) {
        if (!AlmostUnified.getStartupConfig().isServerOnly()) {
            eventBus.addListener(this::onRegisterClientSyncRecipe);
        }

        eventBus.addListener(this::onCommonSetup);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::onUnifyLootTable);
    }

    private void onUnifyLootTable(LootTableLoadEvent event) {
        LootUnification.unifyLoot(event.getName(), event.getTable());
    }

    private void onRegisterClientSyncRecipe(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.RECIPE_SERIALIZER)) {
            Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                    ClientRecipeTracker.ID,
                    ClientRecipeTracker.SERIALIZER);
        }

        if (event.getRegistryKey().equals(Registries.RECIPE_TYPE)) {
            Registry.register(BuiltInRegistries.RECIPE_TYPE, ClientRecipeTracker.ID, ClientRecipeTracker.TYPE);
        }
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> PluginManager.initialize(getPlugins()));
    }

    private Collection<AlmostUnifiedPlugin> getPlugins() {
        Collection<Class<AlmostUnifiedPlugin>> pluginClasses = getPluginClasses();
        List<AlmostUnifiedPlugin> plugins = new ArrayList<>();
        try {
            for (Class<AlmostUnifiedPlugin> pluginClass : pluginClasses) {
                plugins.add(pluginClass.getConstructor().newInstance());
            }
        } catch (Exception e) {
            AlmostUnified.LOG.error("Failed to create plugin, while loading it: ", e);
        }

        return plugins;
    }

    private Collection<Class<AlmostUnifiedPlugin>> getPluginClasses() {
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
    private Class<AlmostUnifiedPlugin> getPluginClass(String className) {
        try {
            Class<?> pluginClass = Class.forName(className);
            if (AlmostUnifiedPlugin.class.isAssignableFrom(pluginClass)) {
                //noinspection unchecked
                return (Class<AlmostUnifiedPlugin>) pluginClass;
            }

            AlmostUnified.LOG.error("Failed to load AlmostUnified plugin: " + className + " does not implement " +
                                    AlmostUnifiedPlugin.class.getName());
        } catch (ClassNotFoundException e) {
            AlmostUnified.LOG.error("Failed to load AlmostUnified plugin: " + className, e);
            return null;
        }

        return null;
    }
}

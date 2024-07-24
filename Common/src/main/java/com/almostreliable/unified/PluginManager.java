package com.almostreliable.unified;

import com.almostreliable.unified.api.RecipeUnifierRegistry;
import com.almostreliable.unified.api.plugin.AlmostUnifiedPlugin;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class PluginManager {

    @Nullable private static PluginManager INSTANCE;
    private final List<AlmostUnifiedPlugin> plugins;

    private PluginManager(List<AlmostUnifiedPlugin> plugins) {
        this.plugins = plugins;
    }

    @SuppressWarnings("StaticVariableUsedBeforeInitialization")
    public static PluginManager instance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("PluginManager is not initialized.");
        }

        return INSTANCE;
    }

    static void init(Collection<AlmostUnifiedPlugin> plugins) {
        if (INSTANCE != null) {
            throw new IllegalStateException("PluginManager is already initialized.");
        }

        var sortedPlugins = new ArrayList<>(plugins);
        sortedPlugins.sort((a, b) -> {
            if (a.getPluginId().getNamespace().equals(BuildConfig.MOD_ID)) {
                return -1;
            }

            if (b.getPluginId().getNamespace().equals(BuildConfig.MOD_ID)) {
                return 1;
            }

            return a.getPluginId().compareTo(b.getPluginId());
        });

        String ids = sortedPlugins
                .stream()
                .map(AlmostUnifiedPlugin::getPluginId)
                .map(ResourceLocation::toString)
                .collect(Collectors.joining(", "));
        AlmostUnified.LOGGER.info("Loaded plugins: {}", ids);

        INSTANCE = new PluginManager(sortedPlugins);
    }

    public void registerRecipeUnifiers(RecipeUnifierRegistry registry) {
        forEachPlugin(plugin -> plugin.registerRecipeUnifiers(registry));
    }

    public void forEachPlugin(Consumer<AlmostUnifiedPlugin> consumer) {
        var it = plugins.listIterator();
        while (it.hasNext()) {
            AlmostUnifiedPlugin plugin = it.next();
            try {
                consumer.accept(plugin);
            } catch (Exception e) {
                it.remove();
                AlmostUnified.LOGGER.error("Failed to process plugin {}, removing it.", plugin.getPluginId(), e);
            }
        }
    }
}

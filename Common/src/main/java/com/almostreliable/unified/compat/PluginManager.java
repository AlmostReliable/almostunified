package com.almostreliable.unified.compat;

import com.almostreliable.unified.AlmostUnifiedCommon;
import com.almostreliable.unified.api.constant.ModConstants;
import com.almostreliable.unified.api.plugin.AlmostUnifiedPlugin;
import com.almostreliable.unified.api.unification.recipe.RecipeUnifierRegistry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

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
            throw new IllegalStateException("PluginManager is not initialized");
        }

        return INSTANCE;
    }

    public static void init(Collection<AlmostUnifiedPlugin> plugins) {
        if (INSTANCE != null) {
            throw new IllegalStateException("PluginManager is already initialized");
        }

        var sortedPlugins = new ArrayList<>(plugins);
        sortedPlugins.sort((a, b) -> {
            if (a.getPluginId().getNamespace().equals(ModConstants.ALMOST_UNIFIED)) {
                return -1;
            }

            if (b.getPluginId().getNamespace().equals(ModConstants.ALMOST_UNIFIED)) {
                return 1;
            }

            return a.getPluginId().compareTo(b.getPluginId());
        });

        String ids = sortedPlugins
            .stream()
            .map(AlmostUnifiedPlugin::getPluginId)
            .map(ResourceLocation::toString)
            .collect(Collectors.joining(", "));
        AlmostUnifiedCommon.LOGGER.info("Loaded plugins: {}", ids);

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
                AlmostUnifiedCommon.LOGGER.error("Failed to process plugin {}, removing it.", plugin.getPluginId(), e);
            }
        }
    }
}

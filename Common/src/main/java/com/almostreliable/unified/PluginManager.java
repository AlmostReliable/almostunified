package com.almostreliable.unified;

import com.almostreliable.unified.api.UnifierRegistry;
import com.almostreliable.unified.api.plugin.AlmostUnifiedPlugin;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PluginManager {

    @Nullable
    private static PluginManager pluginManager;
    private final List<AlmostUnifiedPlugin> plugins;

    private PluginManager(List<AlmostUnifiedPlugin> plugins) {
        this.plugins = plugins;
    }

    public static PluginManager instance() {
        if (pluginManager == null) {
            throw new IllegalStateException("PluginManager is not initialized");
        }

        return pluginManager;
    }

    static void initialize(Collection<AlmostUnifiedPlugin> plugins) {
        if (pluginManager != null) {
            throw new IllegalStateException("PluginManager is already initialized");
        }

        var p = new ArrayList<>(plugins);
        p.sort((a, b) -> {
            if (a.getPluginId().getNamespace().equals(BuildConfig.MOD_ID)) {
                return -1;
            }

            if (b.getPluginId().getNamespace().equals(BuildConfig.MOD_ID)) {
                return 1;
            }

            return a.getPluginId().compareTo(b.getPluginId());
        });

        String ids = p
                .stream()
                .map(AlmostUnifiedPlugin::getPluginId)
                .map(ResourceLocation::toString)
                .collect(Collectors.joining(", "));
        AlmostUnified.LOG.info("Loaded AlmostUnified plugins: " + ids);
        pluginManager = new PluginManager(p);
    }

    public void registerUnifiers(UnifierRegistry registry) {
        forEachPlugin(p -> p.registerUnifiers(registry));
    }

    public void forEachPlugin(Consumer<AlmostUnifiedPlugin> consumer) {
        var it = plugins.listIterator();
        while (it.hasNext()) {
            AlmostUnifiedPlugin p = it.next();
            try {
                consumer.accept(p);
            } catch (Exception t) {
                it.remove();
                AlmostUnified.LOG.error("Failed to process plugin " + p.getPluginId() + ", removing it", t);
            }
        }
    }
}

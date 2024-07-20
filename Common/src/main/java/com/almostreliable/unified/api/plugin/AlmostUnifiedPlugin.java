package com.almostreliable.unified.api.plugin;

import com.almostreliable.unified.api.UnifierRegistry;
import com.almostreliable.unified.api.recipe.RecipeUnifier;
import net.minecraft.resources.ResourceLocation;

/**
 * Implemented by plugins that wish to register their own {@link RecipeUnifier}s.
 * <p>
 * NeoForge plugins should attach the {@link AlmostUnifiedNeoPlugin} annotation for discovery.<br>
 * Fabric plugins should use the {@code almostunified} entrypoint.
 */
public interface AlmostUnifiedPlugin {

    /**
     * Returns the identifier of the plugin.
     * <p>
     * If your mod has multiple plugins for different modules, make
     * sure they are unique.
     *
     * @return the plugin id
     */
    ResourceLocation getPluginId();

    /**
     * Allows registration of custom {@link RecipeUnifier}s.
     *
     * @param registry the {@link UnifierRegistry} to register with
     */
    default void registerUnifiers(UnifierRegistry registry) {}
}

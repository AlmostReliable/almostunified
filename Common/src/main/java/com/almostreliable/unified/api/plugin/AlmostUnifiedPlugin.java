package com.almostreliable.unified.api.plugin;

import com.almostreliable.unified.api.UnifierRegistry;
import net.minecraft.resources.ResourceLocation;

public interface AlmostUnifiedPlugin {

    ResourceLocation getPluginId();

    default void registerUnifiers(UnifierRegistry registry) {

    }
}

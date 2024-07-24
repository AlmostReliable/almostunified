package com.almostreliable.unified;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.nio.file.Path;
import java.util.Collection;
import java.util.ServiceLoader;
import java.util.Set;

public interface AlmostUnifiedPlatform {

    AlmostUnifiedPlatform INSTANCE = ServiceLoader.load(AlmostUnifiedPlatform.class)
            .findFirst()
            .orElseThrow(() -> new NullPointerException("Failed to load platform service."));

    /**
     * Gets the current platform
     *
     * @return The current platform.
     */
    Platform getPlatform();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    boolean isClient();

    Path getConfigPath();

    Path getLogPath();

    Set<TagKey<Item>> getStoneStrataTags(Collection<String> stoneStrataIds);

    enum Platform {
        NEO_FORGE,
        FABRIC
    }
}

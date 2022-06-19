package com.almostreliable.unified.api;

import com.almostreliable.unified.TagMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.Collection;

public interface ReplacementFallbackStrategy {
    /**
     * Determine a fallback for the given item.
     *
     * @param tag            the tag to replace the item with
     * @param potentialItems the potential items to replace with
     * @param tags           the tag map to use for lookup
     * @return the fallback item, or null if no fallback is available
     * @throws IllegalStateException if returning the lookupItem
     */
    @Nullable
    ResourceLocation getFallback(TagKey<Item> tag, Collection<ResourceLocation> potentialItems, TagMap tags);
}

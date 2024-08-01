package com.almostreliable.unified.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * Helper to get the stone variant of an item.
 * <p>
 * Upon creation, this lookup will try to fetch the stone variant from the
 * {@code c:ores_in_ground} tag. If the tag is present, it will always take priority.
 * <p>
 * As a fallback, it will lazily try to fetch the stone variant from the item or
 * the respective block id.
 */
public interface StoneVariantLookup {

    /**
     * Returns the stone variant for the given item.
     * <p>
     * This assumes that the item has a valid ore tag.<br>
     * Use {@link #isOreTag(TagKey)} to ensure this requirement.
     * <p>
     * If the detected variant is stone, an empty string will be returned.
     *
     * @param item the item to get the stone variant from
     * @return the stone variant of the item
     */
    String getStoneVariant(ResourceLocation item);

    /**
     * Checks if the given tag is an ore tag.
     *
     * @param tag the tag to check
     * @return true if the tag is an ore tag, false otherwise
     */
    boolean isOreTag(TagKey<Item> tag);
}

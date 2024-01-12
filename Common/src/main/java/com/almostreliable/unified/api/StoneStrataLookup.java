package com.almostreliable.unified.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public interface StoneStrataLookup {

    /**
     * Returns the stone strata from the given item. Assumes that the item has a stone strata tag.
     * Use {@link #isStoneStrataTag(TagKey)} to ensure this requirement.
     *
     * @param item The item to get the stone strata from.
     * @return The stone strata of the item. Clean stone strata returns an empty string for later sorting as a
     * fallback variant.
     */
    String getStoneStrata(ResourceLocation item);

    boolean isStoneStrataTag(TagKey<Item> tag);
}

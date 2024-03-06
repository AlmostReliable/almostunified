package com.almostreliable.unified;

import com.almostreliable.unified.api.*;
import com.almostreliable.unified.utils.ReplacementMapImpl;
import com.almostreliable.unified.utils.TagMapImpl;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.Collection;
import java.util.Map;

/**
 * Holder class for storing all the data needed for replacements in recipes.
 *
 * @param globalTagMap      The global tag map, containing all tags.
 * @param filteredTagMap    The filtered tag map, containing only the tags that will be used for replacing. Determined by the unify config.
 * @param stoneStrataLookup The stone strata handler, used for replacing stone strata.
 * @param replacementMap    The replacement map, used for replacing items.
 */
public record ReplacementData(TagMap<Item> globalTagMap, TagMap<Item> filteredTagMap,
                              StoneStrataLookup stoneStrataLookup,
                              ReplacementMap replacementMap) {

    public static ReplacementData load(Map<ResourceLocation, Collection<Holder<Item>>> tags, UnifySettings unifySettings, TagOwnerships tagOwnerships) {
        var globalTagMap = TagMapImpl.createFromItemTags(tags);
        var filteredTagMap = globalTagMap.filtered(unifySettings.getTags()::contains, unifySettings::shouldIncludeItem);

        var stoneStrata = StoneStrataLookupImpl.create(
                unifySettings.getStoneStrata(),
                AlmostUnifiedPlatform.INSTANCE.getStoneStrataTags(unifySettings.getStoneStrata()),
                globalTagMap
        );

        var replacementMap = new ReplacementMapImpl(unifySettings.getModPriorities(),
                filteredTagMap,
                stoneStrata,
                tagOwnerships);

        return new ReplacementData(globalTagMap, filteredTagMap, stoneStrata, replacementMap);
    }
}

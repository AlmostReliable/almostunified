package com.almostreliable.unified;

import com.almostreliable.unified.api.StoneStrataHandler;
import com.almostreliable.unified.config.UnifyConfig;
import com.almostreliable.unified.utils.ReplacementMap;
import com.almostreliable.unified.utils.TagMap;
import com.almostreliable.unified.utils.TagOwnerships;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.Collection;
import java.util.Map;

/**
 * Holder class for storing all the data needed for replacements in recipes.
 *
 * @param globalTagMap       The global tag map, containing all tags.
 * @param filteredTagMap     The filtered tag map, containing only the tags that will be used for replacing. Determined by the unify config.
 * @param stoneStrataHandler The stone strata handler, used for replacing stone strata.
 * @param replacementMap     The replacement map, used for replacing items.
 */
public record ReplacementData(TagMap<Item> globalTagMap, TagMap<Item> filteredTagMap,
                              StoneStrataHandler stoneStrataHandler,
                              ReplacementMap replacementMap) {

    public static ReplacementData load(Map<ResourceLocation, Collection<Holder<Item>>> tags, UnifyConfig unifyConfig, TagOwnerships tagOwnerships) {
        var globalTagMap = TagMap.createFromItemTags(tags);
        var unifyTags = unifyConfig.bakeAndValidateTags(tags);
        var filteredTagMap = globalTagMap.filtered(unifyTags::contains, unifyConfig::includeItem);

        var stoneStrataHandler = StoneStrataHandler.create(
                unifyConfig.getStoneStrata(),
                AlmostUnifiedPlatform.INSTANCE.getStoneStrataTags(unifyConfig.getStoneStrata()),
                globalTagMap
        );

        var replacementMap = new ReplacementMap(unifyConfig, filteredTagMap, stoneStrataHandler, tagOwnerships);

        return new ReplacementData(globalTagMap, filteredTagMap, stoneStrataHandler, replacementMap);
    }
}

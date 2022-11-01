package com.almostreliable.unified.compat;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.AlmostUnifiedPlatform;
import com.almostreliable.unified.api.StoneStrataHandler;
import com.almostreliable.unified.config.UnifyConfig;
import com.almostreliable.unified.utils.ReplacementMap;
import com.almostreliable.unified.utils.TagMap;
import com.almostreliable.unified.utils.UnifyTag;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class HideHelper {

    public static Collection<ItemStack> createHidingList(UnifyConfig config) {
        List<UnifyTag<Item>> unifyTags = config.bakeTags();
        TagMap filteredTagMap = TagMap.create(unifyTags);
        StoneStrataHandler stoneStrataHandler = getStoneStrataHandler(config);
        ReplacementMap repMap = new ReplacementMap(filteredTagMap, stoneStrataHandler, config);

        return filteredTagMap.getTags().stream().map(unifyTag -> {
            Collection<ResourceLocation> itemsByTag = filteredTagMap.getItems(unifyTag);
            if (itemsByTag.size() <= 1) return new ArrayList<ItemStack>();

            Set<ResourceLocation> replacements = itemsByTag
                    .stream()
                    .map(repMap::getReplacementForItem)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            List<ResourceLocation> toHide = itemsByTag.stream().filter(rl -> !replacements.contains(rl)).toList();

            if (!toHide.isEmpty()) {
                AlmostUnified.LOG.info("Hiding {}/{} items for tag {} -> {}",
                        toHide.size(),
                        itemsByTag.size(),
                        unifyTag.location(),
                        toHide);
            }

            return toHide.stream().flatMap(rl -> Registry.ITEM.getOptional(rl).stream()).map(ItemStack::new).toList();
        }).flatMap(Collection::stream).toList();
    }

    private static StoneStrataHandler getStoneStrataHandler(UnifyConfig config) {
        Set<UnifyTag<Item>> stoneStrataTags = AlmostUnifiedPlatform.INSTANCE.getStoneStrataTags(config.getStoneStrata());
        TagMap stoneStrataTagMap = TagMap.create(stoneStrataTags);
        return StoneStrataHandler.create(config.getStoneStrata(), stoneStrataTags, stoneStrataTagMap);
    }
}

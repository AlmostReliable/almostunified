package com.almostreliable.unified.compat;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.AlmostUnifiedRuntime;
import com.almostreliable.unified.utils.ReplacementMap;
import com.almostreliable.unified.utils.TagMap;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class HideHelper {

    public static Collection<ItemStack> createHidingList(AlmostUnifiedRuntime runtime) {
        ReplacementMap repMap = runtime.getReplacementMap().orElse(null);
        TagMap tagMap = runtime.getFilteredTagMap().orElse(null);

        if (repMap == null || tagMap == null) return new ArrayList<>();

        Set<ResourceLocation> hidingList = new HashSet<>();

        for (var unifyTag : tagMap.getTags()) {
            var itemsByTag = tagMap.getItemsByTag(unifyTag);

            // avoid hiding single entries and tags that only contain the same namespace for all items
            long namespaces = itemsByTag.stream().map(ResourceLocation::getNamespace).distinct().count();
            if (namespaces <= 1) continue;

            Set<ResourceLocation> replacements = new HashSet<>();
            for (ResourceLocation item : itemsByTag) {
                replacements.add(getReplacementForItem(repMap, item));
            }

            Set<ResourceLocation> toHide = new HashSet<>();
            for (ResourceLocation item : itemsByTag) {
                if (!replacements.contains(item)) {
                    toHide.add(item);
                }
            }

            if (toHide.isEmpty()) continue;

            AlmostUnified.LOG.info(
                    "Hiding {}/{} items for tag {} -> {}",
                    toHide.size(),
                    itemsByTag.size(),
                    unifyTag.location(),
                    toHide
            );

            hidingList.addAll(toHide);
        }

        hidingList.addAll(getRefItems(repMap));

        return hidingList
                .stream()
                .flatMap(rl -> Registry.ITEM.getOptional(rl).stream())
                .map(ItemStack::new)
                .collect(Collectors.toList());
    }

    /**
     * Returns the replacement for the given item, or the item itself if no replacement is found.
     * <p>
     * Returning the item itself is important for stone strata detection.
     *
     * @param repMap The replacement map.
     * @param item   The item to get the replacement for.
     * @return The replacement for the given item, or the item itself if no replacement is found.
     */
    private static ResourceLocation getReplacementForItem(ReplacementMap repMap, ResourceLocation item) {
        var replacement = repMap.getReplacementForItem(item);
        if (replacement == null) return item;
        return replacement;
    }

    /**
     * Returns a set of all items that are contained in the reference tags.
     *
     * @return A set of all items that are contained in the reference tags.
     */
    private static Set<ResourceLocation> getRefItems(ReplacementMap repMap) {
        Set<ResourceLocation> hidingList = new HashSet<>();

        for (var ref : AlmostUnified.getTagOwnerships().getRefs()) {
            var owner = AlmostUnified.getTagOwnerships().getOwnerByTag(ref);
            assert owner != null;

            var dominantItem = repMap.getPreferredItemForTag(owner, $ -> true);

            TagKey<Item> asTagKey = TagKey.create(Registry.ITEM_REGISTRY, ref.location());
            Set<ResourceLocation> refItems = new HashSet<>();
            Registry.ITEM.getTagOrEmpty(asTagKey).forEach(holder -> {
                ResourceLocation item = Registry.ITEM.getKey(holder.value());
                if (item.equals(dominantItem)) return; // don't hide if the item is a dominant one
                refItems.add(item);
            });

            if (refItems.isEmpty()) continue;

            AlmostUnified.LOG.info(
                    "Hiding reference tag #{} of owner tag #{} -> {}",
                    ref.location(),
                    owner.location(),
                    refItems
            );

            hidingList.addAll(refItems);
        }

        return hidingList;
    }
}

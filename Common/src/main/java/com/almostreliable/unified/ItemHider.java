package com.almostreliable.unified;

import com.almostreliable.unified.api.UnifyHandler;
import com.almostreliable.unified.api.UnifyLookup;
import com.almostreliable.unified.utils.Utils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ItemHider {

    public static final TagKey<Item> HIDE_TAG = TagKey.create(Registries.ITEM,
            new ResourceLocation(BuildConfig.MOD_ID, "hide"));

    public static void applyHideTags(Map<ResourceLocation, Collection<Holder<Item>>> tags, Collection<UnifyHandler> handlers) {
        for (var handler : handlers) {
            if (handler.hideNonPreferredItemsInRecipeViewers()) {
                ItemHider.applyHideTags(tags, handler);
            }
        }
    }

    public static void applyHideTags(Map<ResourceLocation, Collection<Holder<Item>>> tags, UnifyHandler handler) {
        var hidingItems = createHidingItems(handler);

        Collection<Holder<Item>> itemsToHide = new HashSet<>(hidingItems.size());
        for (Item hidingItem : hidingItems) {
            itemsToHide.add(BuiltInRegistries.ITEM.wrapAsHolder(hidingItem));
        }

        Collection<Holder<Item>> existing = tags.get(HIDE_TAG.location());
        if (existing != null) {
            itemsToHide.addAll(existing);
        }

        tags.put(HIDE_TAG.location(), itemsToHide);
    }

    public static Set<Item> createHidingItems(UnifyHandler handler) {
        Set<ResourceLocation> hidings = new HashSet<>();

        for (TagKey<Item> tag : handler.getTagMap().getTags()) {
            var itemsByTag = handler.getTagMap().getEntriesByTag(tag);

            // avoid handling single entries and tags that only contain the same namespace for all items
            if (Utils.allSameNamespace(itemsByTag)) continue;

            Set<ResourceLocation> replacements = new HashSet<>();
            for (ResourceLocation item : itemsByTag) {
                replacements.add(getReplacementForItem(handler, item));
            }

            Set<ResourceLocation> toHide = new HashSet<>();
            for (ResourceLocation item : itemsByTag) {
                if (!replacements.contains(item)) {
                    toHide.add(item);
                }
            }

            if (toHide.isEmpty()) continue;

            AlmostUnified.LOG.info(
                    "[AutoHiding] Hiding {}/{} items for tag '#{}' -> {}",
                    toHide.size(),
                    itemsByTag.size(),
                    tag.location(),
                    toHide
            );

            hidings.addAll(toHide);
        }

        return idsToItems(hidings);
    }

    private static Set<Item> idsToItems(Set<ResourceLocation> ids) {
        return ids.stream().flatMap(id -> BuiltInRegistries.ITEM.getOptional(id).stream()).collect(Collectors.toSet());
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
    private static ResourceLocation getReplacementForItem(UnifyLookup repMap, ResourceLocation item) {
        var replacement = repMap.getReplacementForItem(item);
        if (replacement == null) return item;
        return replacement;
    }
}

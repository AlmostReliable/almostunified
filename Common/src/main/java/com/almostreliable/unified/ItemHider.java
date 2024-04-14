package com.almostreliable.unified;

import com.almostreliable.unified.api.UnifyEntry;
import com.almostreliable.unified.api.UnifyHandler;
import com.almostreliable.unified.api.UnifyLookup;
import com.almostreliable.unified.utils.Utils;
import com.almostreliable.unified.utils.VanillaTagWrapper;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ItemHider {

    public static final TagKey<Item> HIDE_TAG = TagKey.create(Registries.ITEM,
            new ResourceLocation(BuildConfig.MOD_ID, "hide"));

    public static void applyHideTags(VanillaTagWrapper<Item> tags, Collection<UnifyHandler> handlers) {
        for (var handler : handlers) {
            if (handler.hideNonPreferredItemsInRecipeViewers()) {
                ItemHider.applyHideTags(tags, handler);
            }
        }
    }

    public static void applyHideTags(VanillaTagWrapper<Item> tags, UnifyHandler handler) {
        var holdersToHide = createHidingItems(handler);
        for (Holder<Item> holder : holdersToHide) {
            tags.add(HIDE_TAG.location(), holder);
        }
    }

    public static Set<Holder<Item>> createHidingItems(UnifyHandler handler) {
        Set<Holder<Item>> hidings = new HashSet<>();

        for (TagKey<Item> tag : handler.getUnifiedTags()) {
            var entriesByTag = handler.getEntries(tag);

            // avoid handling single entries and tags that only contain the same namespace for all items
            if (Utils.allSameNamespace(entriesByTag)) continue;

            Set<UnifyEntry<Item>> replacements = new HashSet<>();
            for (var holder : entriesByTag) {
                replacements.add(getReplacementForItem(handler, holder));
            }

            Set<Holder<Item>> toHide = new HashSet<>();
            Set<String> toHideIds = new HashSet<>();
            for (var entry : entriesByTag) {
                if (!replacements.contains(entry)) {
                    toHide.add(entry.asHolder());
                    toHideIds.add(entry.id().toString());
                }
            }

            if (toHide.isEmpty()) continue;

            AlmostUnified.LOG.info(
                    "[AutoHiding] Hiding {}/{} items for tag '#{}' -> {}",
                    toHide.size(),
                    entriesByTag.size(),
                    tag.location(),
                    toHideIds
            );

            hidings.addAll(toHide);
        }

        return hidings;
    }

    /**
     * Returns the replacement for the given item, or the item itself if no replacement is found.
     * <p>
     * Returning the item itself is important for stone strata detection.
     *
     * @param repMap The replacement map.
     * @param entry  The holder to get the replacement for.
     * @return The replacement for the given item, or the item itself if no replacement is found.
     */
    private static UnifyEntry<Item> getReplacementForItem(UnifyLookup repMap, UnifyEntry<Item> entry) {
        var replacement = repMap.getReplacementForItem(entry);
        if (replacement == null) return entry;
        return replacement;
    }
}

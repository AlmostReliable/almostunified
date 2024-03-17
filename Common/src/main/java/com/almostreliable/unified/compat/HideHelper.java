package com.almostreliable.unified.compat;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.AlmostUnifiedRuntime;
import com.almostreliable.unified.utils.ReplacementMap;
import com.almostreliable.unified.utils.TagOwnerships;
import com.almostreliable.unified.utils.UnifyTag;
import com.almostreliable.unified.utils.Utils;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class HideHelper {

    private HideHelper() {}

    public static Multimap<UnifyTag<Item>, ResourceLocation> createHidingMap() {
        AlmostUnifiedRuntime runtime = AlmostUnified.getRuntime();
        ReplacementMap repMap = runtime.getReplacementMap().orElse(null);
        var tagMap = runtime.getFilteredTagMap().orElse(null);

        Multimap<UnifyTag<Item>, ResourceLocation> hidingMap = HashMultimap.create();
        if (repMap == null || tagMap == null) return hidingMap;
        TagOwnerships ownerships = repMap.getTagOwnerships();

        for (var unifyTag : tagMap.getTags()) {
            var itemsByTag = tagMap.getEntriesByTag(unifyTag);
            if (Utils.allSameNamespace(itemsByTag)) continue;

            // it's not enough to exclude the preferred item from hiding because it would hide stone stratas
            Set<ResourceLocation> replacements = new HashSet<>();
            for (ResourceLocation item : itemsByTag) {
                ResourceLocation replacement = repMap.getReplacementForItem(item);
                replacements.add(replacement == null ? item : replacement);
            }

            Set<ResourceLocation> itemsToHide = getItemsToHide(unifyTag, itemsByTag, replacements);
            if (itemsToHide != null) {
                hidingMap.putAll(unifyTag, itemsToHide);
            }

            Set<ResourceLocation> refItemsToHide = getRefItemsToHide(unifyTag, ownerships, replacements);
            if (!refItemsToHide.isEmpty()) {
                hidingMap.putAll(unifyTag, refItemsToHide);
            }
        }

        return hidingMap;
    }

    @Nullable
    private static Set<ResourceLocation> getItemsToHide(UnifyTag<Item> unifyTag, Set<ResourceLocation> itemsByTag, Set<ResourceLocation> replacements) {
        Set<ResourceLocation> itemsToHide = new HashSet<>();
        for (ResourceLocation item : itemsByTag) {
            if (!replacements.contains(item)) {
                itemsToHide.add(item);
            }
        }

        if (itemsToHide.isEmpty()) return null;

        AlmostUnified.LOG.info(
                "[AutoHiding] Hiding {}/{} items for tag '#{}' -> {}",
                itemsToHide.size(),
                itemsByTag.size(),
                unifyTag.location(),
                itemsToHide
        );
        return itemsToHide;
    }

    private static Set<ResourceLocation> getRefItemsToHide(UnifyTag<Item> unifyTag, TagOwnerships ownerships, Set<ResourceLocation> replacements) {
        var refTags = ownerships.getRefsByOwner(unifyTag);
        Set<ResourceLocation> refItemsToHide = new HashSet<>();

        for (var refTag : refTags) {
            var asTagKey = TagKey.create(Registries.ITEM, refTag.location());

            BuiltInRegistries.ITEM.getTagOrEmpty(asTagKey).forEach(holder -> {
                ResourceLocation item = BuiltInRegistries.ITEM.getKey(holder.value());
                if (replacements.contains(item)) return;
                refItemsToHide.add(item);
            });

            if (refItemsToHide.isEmpty()) continue;

            AlmostUnified.LOG.info(
                    "[AutoHiding] Hiding reference tag '#{}' of owner tag '#{}' -> {}",
                    refTag.location(),
                    unifyTag.location(),
                    refItemsToHide
            );
        }

        return refItemsToHide;
    }

    public static Collection<ItemStack> getStacksToHide() {
        var hidingMap = createHidingMap();
        if (hidingMap.isEmpty()) return List.of();

        return hidingMap
                .entries()
                .stream()
                .flatMap(rl -> BuiltInRegistries.ITEM.getOptional(rl.getValue()).stream())
                .map(ItemStack::new)
                .toList();
    }
}

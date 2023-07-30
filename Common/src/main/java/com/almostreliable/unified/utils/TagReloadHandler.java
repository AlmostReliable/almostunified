package com.almostreliable.unified.utils;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.config.UnifyConfig;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class TagReloadHandler {

    private static final Object LOCK = new Object();

    private static Map<ResourceLocation, Collection<Holder<Item>>> RAW_ITEM_TAGS;
    private static Map<ResourceLocation, Collection<Holder<Block>>> RAW_BLOCK_TAGS;

    public static void initItemTags(Map<ResourceLocation, Collection<Holder<Item>>> rawItemTags) {
        synchronized (LOCK) {
            RAW_ITEM_TAGS = rawItemTags;
        }
    }

    public static void initBlockTags(Map<ResourceLocation, Collection<Holder<Block>>> rawBlockTags) {
        synchronized (LOCK) {
            RAW_BLOCK_TAGS = rawBlockTags;
        }
    }

    public static void run() {
        if (RAW_ITEM_TAGS == null || RAW_BLOCK_TAGS == null) {
            return;
        }

        AlmostUnified.onTagLoaderReload(RAW_ITEM_TAGS);

        RAW_ITEM_TAGS = null;
        RAW_BLOCK_TAGS = null;
    }

    private TagReloadHandler() {}

    // TODO: add logging
    // TODO: block logic
    // TODO: add config
    /*
    {
        "immersive_engineering:crusher_multiblock": [
            "forge:storage_blocks/steel",
            "forge:storage_blocks/iron"
        ]
    }
     */

    // TODO: return boolean with true if something changed to rebuild the tagmaps for the runtime
    public static void applyInheritance(UnifyConfig unifyConfig, TagMap globalTagMap, TagMap filteredTagMap, ReplacementMap repMap) {
        Preconditions.checkNotNull(RAW_ITEM_TAGS, "Item tags were not loaded correctly");
        Preconditions.checkNotNull(RAW_BLOCK_TAGS, "Block tags were not loaded correctly");

        var relations = resolveRelations(filteredTagMap, repMap);

        for (TagRelation relation : relations) {
            var dominantHolder = findDominantHolder(relation);
            if (dominantHolder == null) continue;

            var dominantTags = globalTagMap.getTagsByItem(relation.dominant);

            for (var item : relation.items) {
                var itemTags = globalTagMap.getTagsByItem(item);

                for (var itemTag : itemTags) {
                    if (!unifyConfig.shouldInheritItemTag(itemTag, dominantTags)) continue;

                    var itemTagHolders = RAW_ITEM_TAGS.get(itemTag.location());
                    if (itemTagHolders == null) continue;

                    ImmutableSet.Builder<Holder<Item>> newHolders = ImmutableSet.builder();
                    newHolders.addAll(itemTagHolders);
                    newHolders.add(dominantHolder);

                    RAW_ITEM_TAGS.put(itemTag.location(), newHolders.build());
                }
            }
        }
    }

    private static Set<TagRelation> resolveRelations(TagMap filteredTagMap, ReplacementMap repMap) {
        Set<TagRelation> relations = new HashSet<>();

        for (var unifyTag : filteredTagMap.getTags()) {
            var itemsByTag = filteredTagMap.getItemsByTag(unifyTag);

            // avoid handling single entries and tags that only contain the same namespace for all items
            long namespaces = itemsByTag.stream().map(ResourceLocation::getNamespace).distinct().count();
            if (namespaces <= 1) continue;

            ResourceLocation dominant = repMap.getPreferredItemForTag(unifyTag, $ -> true);
            if (dominant == null || !BuiltInRegistries.ITEM.containsKey(dominant)) continue;

            Set<ResourceLocation> items = itemsByTag.stream()
                    .filter(item -> !item.equals(dominant))
                    .filter(BuiltInRegistries.ITEM::containsKey)
                    .collect(Collectors.toSet());

            if (items.isEmpty()) continue;
            relations.add(new TagRelation(unifyTag.location(), dominant, items));
        }

        return relations;
    }

    @SuppressWarnings("StaticVariableUsedBeforeInitialization")
    @Nullable
    private static Holder<Item> findDominantHolder(TagRelation relation) {
        var tagHolders = RAW_ITEM_TAGS.get(relation.tag);
        if (tagHolders == null) return null;

        for (var tagHolder : tagHolders) {
            var holderKey = tagHolder.unwrapKey();
            if (holderKey.isPresent() && holderKey.get().location().equals(relation.dominant)) {
                return tagHolder;
            }
        }

        return null;
    }

    private record TagRelation(ResourceLocation tag, ResourceLocation dominant, Set<ResourceLocation> items) {}
}

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

    // TODO: return boolean with true if something changed (ONLY IN THE ITEM TAGS) to rebuild the tagmaps for the runtime
    public static void applyInheritance(UnifyConfig unifyConfig, TagMap<Item> globalTagMap, TagMap<Item> filteredTagMap, ReplacementMap repMap) {
        Preconditions.checkNotNull(RAW_ITEM_TAGS, "Item tags were not loaded correctly");
        Preconditions.checkNotNull(RAW_BLOCK_TAGS, "Block tags were not loaded correctly");

        var relations = resolveRelations(filteredTagMap, repMap);
        if (relations.isEmpty()) return;

        var blockTagMap = TagMap.createFromBlockTags(RAW_BLOCK_TAGS);

        for (TagRelation relation : relations) {
            var dominantItemHolder = findDominantItemHolder(relation);
            var dominantBlockHolder = findDominantBlockHolder(blockTagMap, relation.dominant);

            var dominantItemTags = globalTagMap.getTagsByEntry(relation.dominant);

            for (var item : relation.items) {
                if (dominantItemHolder != null) {
                    var itemTags = globalTagMap.getTagsByEntry(item);

                    for (var itemTag : itemTags) {
                        if (!unifyConfig.shouldInheritItemTag(itemTag, dominantItemTags)) continue;

                        var itemTagHolders = RAW_ITEM_TAGS.get(itemTag.location());
                        if (itemTagHolders == null) continue;

                        ImmutableSet.Builder<Holder<Item>> newHolders = ImmutableSet.builder();
                        newHolders.addAll(itemTagHolders);
                        newHolders.add(dominantItemHolder);

                        RAW_ITEM_TAGS.put(itemTag.location(), newHolders.build());
                    }
                }

                if (dominantBlockHolder == null) continue;
                var blockTags = blockTagMap.getTagsByEntry(item);

                for (var blockTag : blockTags) {
                    if (!unifyConfig.shouldInheritBlockTag(blockTag, dominantItemTags)) continue;

                    var blockTagHolders = RAW_BLOCK_TAGS.get(blockTag.location());
                    if (blockTagHolders == null) continue;

                    ImmutableSet.Builder<Holder<Block>> newHolders = ImmutableSet.builder();
                    newHolders.addAll(blockTagHolders);
                    newHolders.add(dominantBlockHolder);

                    RAW_BLOCK_TAGS.put(blockTag.location(), newHolders.build());
                }
            }
        }
    }

    private static Set<TagRelation> resolveRelations(TagMap<Item> filteredTagMap, ReplacementMap repMap) {
        Set<TagRelation> relations = new HashSet<>();

        for (var unifyTag : filteredTagMap.getTags()) {
            var itemsByTag = filteredTagMap.getEntriesByTag(unifyTag);

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
    private static Holder<Item> findDominantItemHolder(TagRelation relation) {
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

    @SuppressWarnings("StaticVariableUsedBeforeInitialization")
    @Nullable
    private static Holder<Block> findDominantBlockHolder(TagMap<Block> tagMap, ResourceLocation dominant) {
        var blockTags = tagMap.getTagsByEntry(dominant);
        if (blockTags.isEmpty()) return null;

        var tagHolders = RAW_BLOCK_TAGS.get(blockTags.iterator().next().location());
        if (tagHolders == null) return null;

        for (var tagHolder : tagHolders) {
            var holderKey = tagHolder.unwrapKey();
            if (holderKey.isPresent() && holderKey.get().location().equals(dominant)) {
                return tagHolder;
            }
        }

        return null;
    }

    private record TagRelation(ResourceLocation tag, ResourceLocation dominant, Set<ResourceLocation> items) {}
}

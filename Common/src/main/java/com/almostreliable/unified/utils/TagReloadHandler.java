package com.almostreliable.unified.utils;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.config.UnifyConfig;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
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

    private TagReloadHandler() {}

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

    public static boolean applyInheritance(UnifyConfig unifyConfig, TagMap<Item> globalTagMap, TagMap<Item> filteredTagMap, ReplacementMap repMap) {
        Preconditions.checkNotNull(RAW_ITEM_TAGS, "Item tags were not loaded correctly");
        Preconditions.checkNotNull(RAW_BLOCK_TAGS, "Block tags were not loaded correctly");

        Multimap<ResourceLocation, ResourceLocation> changedItemTags = HashMultimap.create();
        Multimap<ResourceLocation, ResourceLocation> changedBlockTags = HashMultimap.create();

        var relations = resolveRelations(filteredTagMap, repMap);
        if (relations.isEmpty()) return false;

        var blockTagMap = TagMap.createFromBlockTags(RAW_BLOCK_TAGS);

        for (TagRelation relation : relations) {
            var dominant = relation.dominant;
            var dominantItemHolder = findDominantItemHolder(relation);
            var dominantBlockHolder = findDominantBlockHolder(blockTagMap, dominant);

            var dominantItemTags = globalTagMap.getTagsByEntry(dominant);

            for (var item : relation.items) {
                if (dominantItemHolder != null) {
                    var changed = applyItemTags(unifyConfig, globalTagMap, dominantItemHolder, dominantItemTags, item);
                    changedItemTags.putAll(dominant, changed);
                }
                if (dominantBlockHolder != null) {
                    var changed = applyBlockTags(unifyConfig, blockTagMap, dominantBlockHolder, dominantItemTags, item);
                    changedBlockTags.putAll(dominant, changed);
                }
            }
        }

        if (!changedBlockTags.isEmpty()) {
            changedBlockTags.asMap().forEach((dominant, tags) -> {
                AlmostUnified.LOG.info("[TagInheritance] Added '{}' to block tags {}", dominant, tags);
            });
        }
        if (!changedItemTags.isEmpty()) {
            changedItemTags.asMap().forEach((dominant, tags) -> {
                AlmostUnified.LOG.info("[TagInheritance] Added '{}' to item tags {}", dominant, tags);
            });
            return true;
        }

        return false;
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

    private static Set<ResourceLocation> applyItemTags(UnifyConfig unifyConfig, TagMap<Item> globalTagMap, Holder<Item> dominantItemHolder, Set<UnifyTag<Item>> dominantItemTags, ResourceLocation item) {
        var itemTags = globalTagMap.getTagsByEntry(item);
        Set<ResourceLocation> changed = new HashSet<>();

        for (var itemTag : itemTags) {
            if (!unifyConfig.shouldInheritItemTag(itemTag, dominantItemTags)) continue;

            var itemTagHolders = RAW_ITEM_TAGS.get(itemTag.location());
            if (itemTagHolders == null) continue;

            ImmutableSet.Builder<Holder<Item>> newHolders = ImmutableSet.builder();
            newHolders.addAll(itemTagHolders);
            newHolders.add(dominantItemHolder);

            RAW_ITEM_TAGS.put(itemTag.location(), newHolders.build());
            changed.add(itemTag.location());
        }

        return changed;
    }

    private static Set<ResourceLocation> applyBlockTags(UnifyConfig unifyConfig, TagMap<Block> blockTagMap, Holder<Block> dominantBlockHolder, Set<UnifyTag<Item>> dominantItemTags, ResourceLocation item) {
        var blockTags = blockTagMap.getTagsByEntry(item);
        Set<ResourceLocation> changed = new HashSet<>();

        for (var blockTag : blockTags) {
            if (!unifyConfig.shouldInheritBlockTag(blockTag, dominantItemTags)) continue;

            var blockTagHolders = RAW_BLOCK_TAGS.get(blockTag.location());
            if (blockTagHolders == null) continue;

            ImmutableSet.Builder<Holder<Block>> newHolders = ImmutableSet.builder();
            newHolders.addAll(blockTagHolders);
            newHolders.add(dominantBlockHolder);

            RAW_BLOCK_TAGS.put(blockTag.location(), newHolders.build());
            changed.add(blockTag.location());
        }

        return changed;
    }

    private record TagRelation(ResourceLocation tag, ResourceLocation dominant, Set<ResourceLocation> items) {}
}

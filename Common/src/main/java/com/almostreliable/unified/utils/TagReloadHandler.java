package com.almostreliable.unified.utils;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.ReplacingData;
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
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    public static boolean applyInheritance(UnifyConfig unifyConfig, ReplacingData replacingData) {
        Preconditions.checkNotNull(RAW_ITEM_TAGS, "Item tags were not loaded correctly");
        Preconditions.checkNotNull(RAW_BLOCK_TAGS, "Block tags were not loaded correctly");

        Multimap<ResourceLocation, ResourceLocation> changedItemTags = HashMultimap.create();
        Multimap<ResourceLocation, ResourceLocation> changedBlockTags = HashMultimap.create();

        var relations = resolveRelations(replacingData.filteredTagMap(), replacingData.replacementMap());
        if (relations.isEmpty()) return false;

        var blockTagMap = TagMap.createFromBlockTags(RAW_BLOCK_TAGS);
        var globalTagMap = replacingData.globalTagMap();

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
            if (allSameNamespace(itemsByTag)) continue;

            ResourceLocation dominant = repMap.getPreferredItemForTag(unifyTag, $ -> true);
            if (dominant == null || !BuiltInRegistries.ITEM.containsKey(dominant)) continue;

            Set<ResourceLocation> items = getNonDominantItemsAndValidate(itemsByTag, dominant);

            if (items.isEmpty()) continue;
            relations.add(new TagRelation(unifyTag.location(), dominant, items));
        }

        return relations;
    }

    /**
     * Checks if all ids have the same namespace
     *
     * @param ids set of ids
     * @return true if all ids have the same namespace
     */
    private static boolean allSameNamespace(Set<ResourceLocation> ids) {
        if (ids.size() <= 1) return true;

        var it = ids.iterator();
        var namespace = it.next().getNamespace();

        while (it.hasNext()) {
            if (!it.next().getNamespace().equals(namespace)) return false;
        }

        return true;
    }

    /**
     * Returns a set of all items that are not the dominant item and are valid by checking if they are registered
     *
     * @param itemIds  set of item ids
     * @param dominant dominant item id
     * @return set of all items that are not the dominant item and are valid
     */
    @NotNull
    private static Set<ResourceLocation> getNonDominantItemsAndValidate(Set<ResourceLocation> itemIds, ResourceLocation dominant) {
        Set<ResourceLocation> result = new HashSet<>(itemIds.size());
        for (ResourceLocation id : itemIds) {
            if (!id.equals(dominant) && BuiltInRegistries.ITEM.containsKey(id)) {
                result.add(id);
            }
        }

        return result;
    }

    @SuppressWarnings("StaticVariableUsedBeforeInitialization")
    @Nullable
    private static Holder<Item> findDominantItemHolder(TagRelation relation) {
        var tagHolders = RAW_ITEM_TAGS.get(relation.tag);
        if (tagHolders == null) return null;

        return findDominantHolder(tagHolders, relation.dominant);
    }

    @SuppressWarnings("StaticVariableUsedBeforeInitialization")
    @Nullable
    private static Holder<Block> findDominantBlockHolder(TagMap<Block> tagMap, ResourceLocation dominant) {
        var blockTags = tagMap.getTagsByEntry(dominant);
        if (blockTags.isEmpty()) return null;

        var tagHolders = RAW_BLOCK_TAGS.get(blockTags.iterator().next().location());
        if (tagHolders == null) return null;

        return findDominantHolder(tagHolders, dominant);
    }

    @Nullable
    private static <T> Holder<T> findDominantHolder(Collection<Holder<T>> holders, ResourceLocation dominant) {
        for (var tagHolder : holders) {
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
            if (tryUpdatingRawTags(dominantItemHolder, itemTag, RAW_ITEM_TAGS)) {
                changed.add(itemTag.location());
            }
        }

        return changed;
    }

    private static Set<ResourceLocation> applyBlockTags(UnifyConfig unifyConfig, TagMap<Block> blockTagMap, Holder<Block> dominantBlockHolder, Set<UnifyTag<Item>> dominantItemTags, ResourceLocation item) {
        var blockTags = blockTagMap.getTagsByEntry(item);
        Set<ResourceLocation> changed = new HashSet<>();

        for (var blockTag : blockTags) {
            if (!unifyConfig.shouldInheritBlockTag(blockTag, dominantItemTags)) continue;
            if (tryUpdatingRawTags(dominantBlockHolder, blockTag, RAW_BLOCK_TAGS)) {
                changed.add(blockTag.location());
            }
        }

        return changed;
    }

    private static <T> boolean tryUpdatingRawTags(Holder<T> dominantHolder, UnifyTag<T> tag, Map<ResourceLocation, Collection<Holder<T>>> rawTags) {
        var tagHolders = rawTags.get(tag.location());
        if (tagHolders == null) return false;
        if (tagHolders.contains(dominantHolder)) return false; // already present, no need to add it again

        ImmutableSet.Builder<Holder<T>> newHolders = ImmutableSet.builder();
        newHolders.addAll(tagHolders);
        newHolders.add(dominantHolder);

        rawTags.put(tag.location(), newHolders.build());
        return true;
    }

    private record TagRelation(ResourceLocation tag, ResourceLocation dominant, Set<ResourceLocation> items) {}
}

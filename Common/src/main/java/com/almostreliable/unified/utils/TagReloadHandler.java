package com.almostreliable.unified.utils;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.ReplacementData;
import com.almostreliable.unified.api.ReplacementMap;
import com.almostreliable.unified.api.TagMap;
import com.almostreliable.unified.config.UnifyConfig;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

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

    public static void applyCustomTags(UnifyConfig unifyConfig) {
        Preconditions.checkNotNull(RAW_ITEM_TAGS, "Item tags were not loaded correctly");

        Multimap<ResourceLocation, ResourceLocation> changedItemTags = HashMultimap.create();

        for (var entry : unifyConfig.getCustomTags().entrySet()) {
            ResourceLocation tag = entry.getKey();
            Set<ResourceLocation> itemIds = entry.getValue();

            for (ResourceLocation itemId : itemIds) {
                if (!BuiltInRegistries.ITEM.containsKey(itemId)) {
                    AlmostUnified.LOG.warn("[CustomTags] Custom tag '{}' contains invalid item '{}'", tag, itemId);
                    continue;
                }

                ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, itemId);
                Holder<Item> itemHolder = BuiltInRegistries.ITEM.getHolder(itemKey).orElse(null);
                if (itemHolder == null) continue;

                ImmutableSet.Builder<Holder<Item>> newHolders = ImmutableSet.builder();
                var currentHolders = RAW_ITEM_TAGS.get(tag);

                if (currentHolders != null) {
                    if (currentHolders.contains(itemHolder)) {
                        AlmostUnified.LOG.warn("[CustomTags] Custom tag '{}' already contains item '{}'", tag, itemId);
                        continue;
                    }

                    newHolders.addAll(currentHolders);
                }
                newHolders.add(itemHolder);

                RAW_ITEM_TAGS.put(tag, newHolders.build());
                changedItemTags.put(tag, itemId);
            }
        }

        if (!changedItemTags.isEmpty()) {
            changedItemTags.asMap().forEach((tag, items) -> {
                AlmostUnified.LOG.info("[CustomTags] Modified tag '#{}', added {}", tag, items);
            });
        }
    }

    public static boolean applyInheritance(UnifyConfig unifyConfig, ReplacementData replacementData) {
        Preconditions.checkNotNull(RAW_ITEM_TAGS, "Item tags were not loaded correctly");
        Preconditions.checkNotNull(RAW_BLOCK_TAGS, "Block tags were not loaded correctly");

        Multimap<ResourceLocation, ResourceLocation> changedItemTags = HashMultimap.create();
        Multimap<ResourceLocation, ResourceLocation> changedBlockTags = HashMultimap.create();

        var relations = resolveRelations(replacementData.filteredTagMap(), replacementData.replacementMap());
        if (relations.isEmpty()) return false;

        var blockTagMap = TagMapImpl.createFromBlockTags(RAW_BLOCK_TAGS);
        var globalTagMap = replacementData.globalTagMap();

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
            if (Utils.allSameNamespace(itemsByTag)) continue;

            ResourceLocation dominant = repMap.getPreferredItemForTag(unifyTag);
            if (dominant == null || !BuiltInRegistries.ITEM.containsKey(dominant)) continue;

            Set<ResourceLocation> items = getValidatedItems(itemsByTag, dominant);

            if (items.isEmpty()) continue;
            relations.add(new TagRelation(unifyTag.location(), dominant, items));
        }

        return relations;
    }

    /**
     * Returns a set of all items that are not the dominant item and are valid by checking if they are registered.
     *
     * @param itemIds  The set of all items that are in the tag
     * @param dominant The dominant item
     * @return A set of all items that are not the dominant item and are valid
     */
    private static Set<ResourceLocation> getValidatedItems(Set<ResourceLocation> itemIds, ResourceLocation dominant) {
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

    private static Set<ResourceLocation> applyItemTags(UnifyConfig unifyConfig, TagMap<Item> globalTagMap, Holder<Item> dominantItemHolder, Set<TagKey<Item>> dominantItemTags, ResourceLocation item) {
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

    private static Set<ResourceLocation> applyBlockTags(UnifyConfig unifyConfig, TagMap<Block> blockTagMap, Holder<Block> dominantBlockHolder, Set<TagKey<Item>> dominantItemTags, ResourceLocation item) {
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

    private static <T> boolean tryUpdatingRawTags(Holder<T> dominantHolder, TagKey<T> tag, Map<ResourceLocation, Collection<Holder<T>>> rawTags) {
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

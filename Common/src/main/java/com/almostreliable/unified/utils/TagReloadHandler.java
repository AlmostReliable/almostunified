package com.almostreliable.unified.utils;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.api.*;
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
import java.util.*;

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

    public static void applyCustomTags(Map<ResourceLocation, Set<ResourceLocation>> customTags) {
        Preconditions.checkNotNull(RAW_ITEM_TAGS, "Item tags were not loaded correctly");

        var vanillaTagWrapper = VanillaTagWrapper.of(BuiltInRegistries.ITEM, RAW_ITEM_TAGS);
        Multimap<ResourceLocation, ResourceLocation> changedItemTags = HashMultimap.create();

        for (var entry : customTags.entrySet()) {
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

                var currentHolders = vanillaTagWrapper.get(tag);

                if (!currentHolders.isEmpty()) {
                    if (currentHolders.contains(itemHolder)) {
                        AlmostUnified.LOG.warn("[CustomTags] Custom tag '{}' already contains item '{}'", tag, itemId);
                        continue;
                    }
                }

                vanillaTagWrapper.addHolder(tag, itemHolder);
                changedItemTags.put(tag, itemId);
            }
        }

        if (!changedItemTags.isEmpty()) {
            changedItemTags.asMap().forEach((tag, items) -> {
                AlmostUnified.LOG.info("[CustomTags] Modified tag '#{}', added {}", tag, items);
            });
        }
    }

    public static boolean applyInheritance(TagInheritance<Item> itemTagInheritance, TagInheritance<Block> blockTagInheritance, VanillaTagWrapper<Item> vanillaTags, List<UnifyHandler> unifyHandlers) {
        Preconditions.checkNotNull(RAW_ITEM_TAGS, "Item tags were not loaded correctly");
        Preconditions.checkNotNull(RAW_BLOCK_TAGS, "Block tags were not loaded correctly");

        Multimap<UnifyEntry<Item>, ResourceLocation> changedItemTags = HashMultimap.create();
        Multimap<UnifyEntry<Item>, ResourceLocation> changedBlockTags = HashMultimap.create();

        var relations = resolveRelations(unifyHandlers);
        if (relations.isEmpty()) return false;

        var blockTagMap = VanillaTagWrapper.of(BuiltInRegistries.BLOCK, RAW_BLOCK_TAGS);

        for (TagRelation relation : relations) {
            var dominant = relation.dominant;
            var dominantBlockHolder = findDominantBlockHolder(blockTagMap, dominant);

            var dominantItemTags = vanillaTags
                    .getTags(dominant)
                    .stream()
                    .map(rl -> TagKey.create(Registries.ITEM, rl))
                    .collect(ImmutableSet.toImmutableSet());

            for (var item : relation.items) {
                var appliedItemTags = applyItemTags(itemTagInheritance,
                        vanillaTags,
                        relation.dominant.asHolder(),
                        dominantItemTags,
                        item);
                changedItemTags.putAll(dominant, appliedItemTags);


                if (dominantBlockHolder != null) {
                    var appliedBlockTags = applyBlockTags(blockTagInheritance,
                            blockTagMap,
                            dominantBlockHolder,
                            dominantItemTags,
                            item);
                    changedBlockTags.putAll(dominant, appliedBlockTags);
                }
            }
        }

        if (!changedBlockTags.isEmpty()) {
            changedBlockTags.asMap().forEach((dominant, tags) -> {
                AlmostUnified.LOG.info("[TagInheritance] Added '{}' to block tags {}", dominant.id(), tags);
            });
        }

        if (!changedItemTags.isEmpty()) {
            changedItemTags.asMap().forEach((dominant, tags) -> {
                AlmostUnified.LOG.info("[TagInheritance] Added '{}' to item tags {}", dominant.id(), tags);
            });
            return true;
        }

        return false;
    }

    private static Set<TagRelation> resolveRelations(List<UnifyHandler> unifyHandlers) {
        Set<TagRelation> relations = new HashSet<>();

        for (var handler : unifyHandlers) {
            relations.addAll(resolveRelations(handler.getTagMap(), handler));
        }

        return relations;
    }

    private static Set<TagRelation> resolveRelations(TagMap<Item> filteredTagMap, UnifyLookup repMap) {
        Set<TagRelation> relations = new HashSet<>();

        for (var unifyTag : filteredTagMap.getTags()) {
            var itemsByTag = filteredTagMap.getEntriesByTag(unifyTag);

            // avoid handling single entries and tags that only contain the same namespace for all items
            if (Utils.allSameNamespace(itemsByTag)) continue;

            var dominant = repMap.getPreferredItemForTag(unifyTag);
            if (dominant == null) continue;

            Set<UnifyEntry<Item>> items = removeDominantItem(itemsByTag, dominant);

            if (items.isEmpty()) continue;
            relations.add(new TagRelation(unifyTag, dominant, items));
        }

        return relations;
    }

    /**
     * Returns a set of all items that are not the dominant item and are valid by checking if they are registered.
     *
     * @param holders  The set of all items that are in the tag
     * @param dominant The dominant item
     * @return A set of all items that are not the dominant item and are valid
     */
    private static Set<UnifyEntry<Item>> removeDominantItem(Set<UnifyEntry<Item>> holders, UnifyEntry<Item> dominant) {
        Set<UnifyEntry<Item>> result = new HashSet<>(holders.size());
        for (var holder : holders) {
            if (!holder.equals(dominant)) {
                result.add(holder);
            }
        }

        return result;
    }

    @SuppressWarnings("StaticVariableUsedBeforeInitialization")
    @Nullable
    private static Holder<Block> findDominantBlockHolder(VanillaTagWrapper<Block> tagMap, UnifyEntry<Item> dominant) {
        var blockTags = tagMap.getTags(dominant.id());
        if (blockTags.isEmpty()) return null;

        return BuiltInRegistries.BLOCK.getHolderOrThrow(ResourceKey.create(Registries.BLOCK, dominant.id()));
    }

    private static Set<ResourceLocation> applyItemTags(TagInheritance<Item> tagInheritance, VanillaTagWrapper<Item> tagWrapper, Holder<Item> dominantItemHolder, Set<TagKey<Item>> dominantItemTags, UnifyEntry<Item> item) {
        var itemTags = tagWrapper.getTags(item);
        Set<ResourceLocation> changed = new HashSet<>();

        for (var itemTag : itemTags) {
            var tag = TagKey.create(Registries.ITEM, itemTag);
            if (!tagInheritance.shouldInherit(tag, dominantItemTags)) continue;
            if (tryUpdatingRawTags(dominantItemHolder, tag, tagWrapper)) {
                changed.add(itemTag);
            }
        }

        return changed;
    }

    private static Set<ResourceLocation> applyBlockTags(TagInheritance<Block> tagInheritance, VanillaTagWrapper<Block> blockTagMap, Holder<Block> dominantBlockHolder, Set<TagKey<Item>> dominantItemTags, UnifyEntry<Item> item) {
        var blockTags = blockTagMap.getTags(item.id());
        Set<ResourceLocation> changed = new HashSet<>();

        for (var blockTag : blockTags) {
            var tag = TagKey.create(Registries.BLOCK, blockTag);
            if (!tagInheritance.shouldInherit(tag, dominantItemTags)) continue;
            if (tryUpdatingRawTags(dominantBlockHolder, tag, blockTagMap)) {
                changed.add(blockTag);
            }
        }

        return changed;
    }

    private static <T> boolean tryUpdatingRawTags(Holder<T> dominantHolder, TagKey<T> tag, VanillaTagWrapper<T> tagWrapper) {
        var tagHolders = tagWrapper.get(tag);
        if (tagHolders.contains(dominantHolder)) return false; // already present, no need to add it again

        tagWrapper.addHolder(tag.location(), dominantHolder);
        return true;
    }

    private record TagRelation(TagKey<Item> tag, UnifyEntry<Item> dominant, Set<UnifyEntry<Item>> items) {}
}

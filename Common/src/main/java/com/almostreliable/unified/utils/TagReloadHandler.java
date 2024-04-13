package com.almostreliable.unified.utils;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.api.TagInheritance;
import com.almostreliable.unified.api.UnifyEntry;
import com.almostreliable.unified.api.UnifyLookup;
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
    @Nullable
    private static VanillaTagWrapper<Item> VANILLA_ITEM_TAGS;
    @Nullable
    private static VanillaTagWrapper<Block> VANILLA_BLOCK_TAGS;

    private TagReloadHandler() {}

    public static void initItemTags(Map<ResourceLocation, Collection<Holder<Item>>> rawItemTags) {
        synchronized (LOCK) {
            VANILLA_ITEM_TAGS = VanillaTagWrapper.of(BuiltInRegistries.ITEM, rawItemTags);
        }
    }

    public static void initBlockTags(Map<ResourceLocation, Collection<Holder<Block>>> rawBlockTags) {
        synchronized (LOCK) {
            VANILLA_BLOCK_TAGS = VanillaTagWrapper.of(BuiltInRegistries.BLOCK, rawBlockTags);
        }
    }

    public static void run() {
        if (VANILLA_ITEM_TAGS == null || VANILLA_BLOCK_TAGS == null) {
            return;
        }

        AlmostUnified.onTagLoaderReload(VANILLA_ITEM_TAGS, VANILLA_BLOCK_TAGS);

        VANILLA_ITEM_TAGS.seal();
        VANILLA_BLOCK_TAGS.seal();
        VANILLA_ITEM_TAGS = null;
        VANILLA_BLOCK_TAGS = null;
    }

    public static void applyCustomTags(Map<ResourceLocation, Set<ResourceLocation>> customTags, VanillaTagWrapper<Item> itemTags) {
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

                var currentHolders = itemTags.get(tag);

                if (!currentHolders.isEmpty()) {
                    if (currentHolders.contains(itemHolder)) {
                        AlmostUnified.LOG.warn("[CustomTags] Custom tag '{}' already contains item '{}'", tag, itemId);
                        continue;
                    }
                }

                itemTags.addHolder(tag, itemHolder);
                changedItemTags.put(tag, itemId);
            }
        }

        if (!changedItemTags.isEmpty()) {
            changedItemTags.asMap().forEach((tag, items) -> {
                AlmostUnified.LOG.info("[CustomTags] Modified tag '#{}', added {}", tag, items);
            });
        }
    }

    public static boolean applyInheritance(TagInheritance<Item> itemTagInheritance, TagInheritance<Block> blockTagInheritance, VanillaTagWrapper<Item> itemTags, VanillaTagWrapper<Block> blockTags, List<? extends UnifyLookup> unifyHandlers) {
        Multimap<UnifyEntry<Item>, ResourceLocation> changedItemTags = HashMultimap.create();
        Multimap<UnifyEntry<Item>, ResourceLocation> changedBlockTags = HashMultimap.create();

        var relations = resolveRelations(unifyHandlers, itemTagInheritance, blockTagInheritance);
        if (relations.isEmpty()) return false;

        for (TagRelation relation : relations) {
            var dominant = relation.dominant;
            var dominantBlockHolder = findDominantBlockHolder(blockTags, dominant);

            var dominantItemTags = itemTags
                    .getTags(dominant)
                    .stream()
                    .map(rl -> TagKey.create(Registries.ITEM, rl))
                    .collect(ImmutableSet.toImmutableSet());

            for (var item : relation.items) {
                var appliedItemTags = applyItemTags(itemTagInheritance,
                        itemTags,
                        relation.dominant.asHolder(),
                        dominantItemTags,
                        item);
                changedItemTags.putAll(dominant, appliedItemTags);


                if (dominantBlockHolder != null) {
                    var appliedBlockTags = applyBlockTags(blockTagInheritance,
                            blockTags,
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

    private static Set<TagRelation> resolveRelations(List<? extends UnifyLookup> unifyLookups, TagInheritance<Item> itemTagInheritance, TagInheritance<Block> blockTagInheritance) {
        Set<TagRelation> relations = new HashSet<>();

        for (var handler : unifyLookups) {
            relations.addAll(resolveRelations(handler, itemTagInheritance, blockTagInheritance));
        }

        return relations;
    }

    private static Set<TagRelation> resolveRelations(UnifyLookup lookup, TagInheritance<Item> itemTagInheritance, TagInheritance<Block> blockTagInheritance) {
        Set<TagRelation> relations = new HashSet<>();

        for (var unifyTag : lookup.getUnifiedTags()) {
            if (itemTagInheritance.skipForInheritance(unifyTag) && blockTagInheritance.skipForInheritance(unifyTag)) {
                continue;
            }

            var itemsByTag = lookup.getEntries(unifyTag);

            // avoid handling single entries and tags that only contain the same namespace for all items
            if (Utils.allSameNamespace(itemsByTag)) continue;

            var dominant = lookup.getPreferredItemForTag(unifyTag);
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
    private static Set<UnifyEntry<Item>> removeDominantItem(Collection<UnifyEntry<Item>> holders, UnifyEntry<Item> dominant) {
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

package com.almostreliable.unified.utils;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.*;
import java.util.function.Predicate;

public class TagMap<T> {

    private final Map<UnifyTag<T>, Set<ResourceLocation>> tagsToEntries = new HashMap<>();
    private final Map<ResourceLocation, Set<UnifyTag<T>>> entriesToTags = new HashMap<>();

    protected TagMap() {}

    /**
     * Creates an item tag map from a set of item unify tags.
     * <p>
     * This should only be used for client-side tag maps or for tests.<br>
     * It requires the registry to be loaded in order to validate the tags
     * and fetch the holder from it.
     * <p>
     * For the server, use {@link #createFromItemTags(Map)} instead.
     *
     * @param unifyTags The unify tags.
     * @return A new tag map.
     */
    public static TagMap<Item> create(Set<UnifyTag<Item>> unifyTags) {
        TagMap<Item> tagMap = new TagMap<>();

        unifyTags.forEach(ut -> {
            TagKey<Item> asTagKey = TagKey.create(Registries.ITEM, ut.location());
            BuiltInRegistries.ITEM.getTagOrEmpty(asTagKey).forEach(holder -> {
                ResourceLocation key = BuiltInRegistries.ITEM.getKey(holder.value());
                tagMap.put(ut, key);
            });
        });

        return tagMap;
    }

    /**
     * Creates an item tag map from the vanilla item tag collection passed by the {@link TagLoader}.
     * <p>
     * This should only be used on the server.<br>
     * This tag map should later be filtered by using {@link #filtered(Predicate, Predicate)}.
     * <p>
     * For the client, use {@link #create(Set)} instead.
     *
     * @param tags The vanilla item tag collection.
     * @return A new item tag map.
     */
    public static TagMap<Item> createFromItemTags(Map<ResourceLocation, Collection<Holder<Item>>> tags) {
        TagMap<Item> tagMap = new TagMap<>();

        for (var entry : tags.entrySet()) {
            UnifyTag<Item> unifyTag = UnifyTag.item(entry.getKey());
            for (Holder<?> holder : entry.getValue()) {
                holder
                        .unwrapKey()
                        .map(ResourceKey::location)
                        .filter(BuiltInRegistries.ITEM::containsKey)
                        .ifPresent(itemId -> tagMap.put(unifyTag, itemId));
            }
        }

        return tagMap;
    }

    /**
     * Creates a block tag map from the vanilla block tag collection passed by the {@link TagLoader}.
     * <p>
     * This should only be used on the server.
     *
     * @param tags The vanilla block tag collection.
     * @return A new block tag map.
     */
    public static TagMap<Block> createFromBlockTags(Map<ResourceLocation, Collection<Holder<Block>>> tags) {
        TagMap<Block> tagMap = new TagMap<>();

        for (var entry : tags.entrySet()) {
            UnifyTag<Block> unifyTag = UnifyTag.block(entry.getKey());
            for (Holder<?> holder : entry.getValue()) {
                holder
                        .unwrapKey()
                        .map(ResourceKey::location)
                        .filter(BuiltInRegistries.BLOCK::containsKey)
                        .ifPresent(itemId -> tagMap.put(unifyTag, itemId));
            }
        }

        return tagMap;
    }

    /**
     * Creates a filtered tag map copy.
     *
     * @param tagFilter   A filter to determine which tags to include.
     * @param entryFilter A filter to determine which entries to include.
     * @return A filtered copy of this tag map.
     */
    public TagMap<T> filtered(Predicate<UnifyTag<T>> tagFilter, Predicate<ResourceLocation> entryFilter) {
        TagMap<T> tagMap = new TagMap<>();

        tagsToEntries.forEach((tag, items) -> {
            if (!tagFilter.test(tag)) {
                return;
            }
            items.stream().filter(entryFilter).forEach(item -> tagMap.put(tag, item));
        });

        return tagMap;
    }

    public int tagSize() {
        return tagsToEntries.size();
    }

    public int itemSize() {
        return entriesToTags.size();
    }

    public Set<ResourceLocation> getEntriesByTag(UnifyTag<T> tag) {
        return Collections.unmodifiableSet(tagsToEntries.getOrDefault(tag, Collections.emptySet()));
    }

    public Set<UnifyTag<T>> getTagsByEntry(ResourceLocation entry) {
        return Collections.unmodifiableSet(entriesToTags.getOrDefault(entry, Collections.emptySet()));
    }

    public Set<UnifyTag<T>> getTags() {
        return Collections.unmodifiableSet(tagsToEntries.keySet());
    }

    /**
     * Helper function to build a relationship between a tag and an entry.
     * <p>
     * If the entries don't exist in the internal maps yet, they will be created. That means
     * it needs to be checked whether the tag or entry is valid before calling this method.
     *
     * @param tag   The tag.
     * @param entry The entry.
     */
    protected void put(UnifyTag<T> tag, ResourceLocation entry) {
        tagsToEntries.computeIfAbsent(tag, k -> new HashSet<>()).add(entry);
        entriesToTags.computeIfAbsent(entry, k -> new HashSet<>()).add(tag);
    }
}

package com.almostreliable.unified.utils;

import com.almostreliable.unified.api.TagMap;
import com.google.common.annotations.VisibleForTesting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
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

public class TagMapImpl<T> implements TagMap<T> {

    private final Map<TagKey<T>, Set<ResourceLocation>> tagsToEntries = new HashMap<>();
    private final Map<ResourceLocation, Set<TagKey<T>>> entriesToTags = new HashMap<>();

    @VisibleForTesting
    public TagMapImpl() {}

    /**
     * Creates an item tag map from a set of item unify tags.
     * <p>
     * This should only be used for client-side tag maps or for tests.<br>
     * It requires the registry to be loaded in order to validate the tags
     * and fetch the holder from it.
     * <p>
     * For the server, use {@link #createFromItemTags(Map)} instead.
     *
     * @param tags The unify tags.
     * @return A new tag map.
     */
    public static TagMap<Item> create(Set<TagKey<Item>> tags) {
        TagMapImpl<Item> tagMap = new TagMapImpl<>();

        tags.forEach(tag -> {
            BuiltInRegistries.ITEM.getTagOrEmpty(tag).forEach(holder -> {
                ResourceLocation key = BuiltInRegistries.ITEM.getKey(holder.value());
                tagMap.put(tag, key);
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
        TagMapImpl<Item> tagMap = new TagMapImpl<>();

        for (var entry : tags.entrySet()) {
            var unifyTag = TagKey.create(Registries.ITEM, entry.getKey());
            fillEntries(tagMap, entry.getValue(), unifyTag, BuiltInRegistries.ITEM);
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
        TagMapImpl<Block> tagMap = new TagMapImpl<>();

        for (var entry : tags.entrySet()) {
            var unifyTag = TagKey.create(Registries.BLOCK, entry.getKey());
            fillEntries(tagMap, entry.getValue(), unifyTag, BuiltInRegistries.BLOCK);
        }

        return tagMap;
    }

    /**
     * Unwrap all holders, verify them and put them into the tag map.
     *
     * @param tagMap   The tag map to fill.
     * @param holders  The holders to unwrap.
     * @param unifyTag The unify tag to use.
     * @param registry The registry to use.
     */
    private static <T> void fillEntries(TagMapImpl<T> tagMap, Collection<Holder<T>> holders, TagKey<T> unifyTag, Registry<T> registry) {
        for (var holder : holders) {
            holder
                    .unwrapKey()
                    .map(ResourceKey::location)
                    .filter(registry::containsKey)
                    .ifPresent(id -> tagMap.put(unifyTag, id));
        }
    }

    /**
     * Creates a filtered tag map copy.
     *
     * @param tagFilter   A filter to determine which tags to include.
     * @param entryFilter A filter to determine which entries to include.
     * @return A filtered copy of this tag map.
     */
    @Override
    public TagMap<T> filtered(Predicate<TagKey<T>> tagFilter, Predicate<ResourceLocation> entryFilter) {
        TagMapImpl<T> tagMap = new TagMapImpl<>();

        tagsToEntries.forEach((tag, items) -> {
            if (!tagFilter.test(tag)) {
                return;
            }
            items.stream().filter(entryFilter).forEach(item -> tagMap.put(tag, item));
        });

        return tagMap;
    }

    @Override
    public int tagSize() {
        return tagsToEntries.size();
    }

    @Override
    public int itemSize() {
        return entriesToTags.size();
    }

    @Override
    public Set<ResourceLocation> getEntriesByTag(TagKey<T> tag) {
        return Collections.unmodifiableSet(tagsToEntries.getOrDefault(tag, Collections.emptySet()));
    }

    @Override
    public Set<TagKey<T>> getTagsByEntry(ResourceLocation entry) {
        return Collections.unmodifiableSet(entriesToTags.getOrDefault(entry, Collections.emptySet()));
    }

    @Override
    public Set<TagKey<T>> getTags() {
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
    protected void put(TagKey<T> tag, ResourceLocation entry) {
        tagsToEntries.computeIfAbsent(tag, k -> new HashSet<>()).add(entry);
        entriesToTags.computeIfAbsent(entry, k -> new HashSet<>()).add(tag);
    }
}

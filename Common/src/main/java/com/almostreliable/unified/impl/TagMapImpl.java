package com.almostreliable.unified.impl;

import com.almostreliable.unified.api.TagMap;
import com.almostreliable.unified.api.UnifyEntry;
import com.google.common.annotations.VisibleForTesting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.*;

public class TagMapImpl<T> implements TagMap<T> {

    private final Map<TagKey<T>, Set<UnifyEntry<T>>> tagsToEntries = new HashMap<>();
    private final Map<ResourceLocation, UnifyEntry<T>> idEntriesToTag = new HashMap<>();
    private final Map<Item, UnifyEntry<T>> itemEntriesToTagCache = new HashMap<>();

    @VisibleForTesting
    public TagMapImpl() {}

    public static <T> TagMap<T> compose(List<TagMap<T>> tagMaps) {
        TagMapImpl<T> result = new TagMapImpl<>();
        for (var tagMap : tagMaps) {
            Set<TagKey<T>> tags = tagMap.getTags();
            for (TagKey<T> tag : tags) {
                if (!result.getEntriesByTag(tag).isEmpty()) {
                    throw new IllegalArgumentException("Tag map already contains entries for " + tag);
                }

                for (var holder : tagMap.getEntriesByTag(tag)) {
                    result.put(tag, holder);
                }
            }
        }

        return result;
    }

    @Override
    public int tagSize() {
        return tagsToEntries.size();
    }

    @Override
    public Set<UnifyEntry<T>> getEntriesByTag(TagKey<T> tag) {
        return Collections.unmodifiableSet(tagsToEntries.getOrDefault(tag, Collections.emptySet()));
    }

    @Nullable
    @Override
    public UnifyEntry<T> getEntry(ResourceLocation entry) {
        return idEntriesToTag.get(entry);
    }

    @Nullable
    @Override
    public UnifyEntry<T> getEntry(Item item) {
        return itemEntriesToTagCache.computeIfAbsent(item, i -> getEntry(BuiltInRegistries.ITEM.getKey(i)));
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
     * @param entry The holder entry.
     */
    private void put(TagKey<T> tag, UnifyEntry<T> entry) {
        var entriesForTag = tagsToEntries.computeIfAbsent(tag, k -> new HashSet<>());

//        if (currentTag != null) {
//            AlmostUnified.LOG.warn(
//                    "Item '{}' already has a tag ('#{}') used for unification and can't have multiple tags. This needs to be manually fixed by the user. Further tags will be ignored.",
//                    entry.id(),
//                    currentTag.location());
//            return;
//        }

        entriesForTag.add(entry);
        idEntriesToTag.put(entry.id(), entry);
    }

    public static class Builder<T> {
        private final Map<TagKey<T>, Set<UnifyEntry<T>>> entries = new HashMap<>();
        private final Registry<T> registry;

        public Builder(Registry<T> registry) {
            this.registry = registry;
        }

        public Registry<T> getRegistry() {
            return registry;
        }

        public Builder<T> put(TagKey<T> tag, ResourceLocation... entries) {
            Set<UnifyEntry<T>> holders = this.entries.computeIfAbsent(tag, k -> new HashSet<>());
            for (var entry : entries) {
                holders.add(new UnifyEntryImpl<>(registry, entry));
            }

            return this;
        }

        public Builder<T> put(TagKey<T> tag, String... entries) {
            Set<UnifyEntry<T>> holders = this.entries.computeIfAbsent(tag, k -> new HashSet<>());
            for (var entry : entries) {
                ResourceLocation id = new ResourceLocation(entry);
                holders.add(new UnifyEntryImpl<>(registry, id));
            }

            return this;
        }

        @SafeVarargs
        public final Builder<T> put(TagKey<T> tag, T... entries) {
            Set<UnifyEntry<T>> holders = this.entries.computeIfAbsent(tag, k -> new HashSet<>());
            for (var entry : entries) {
                holders.add(new UnifyEntryImpl<>(registry, entry));
            }

            return this;
        }

        public TagMap<T> build() {
            TagMapImpl<T> tagMap = new TagMapImpl<>();

            entries.forEach((tag, unifyEntries) -> {
                unifyEntries.forEach(entry -> {
                    tagMap.put(tag, entry);
                    ((UnifyEntryImpl<T>) entry).bindTag(tag);
                });
            });

            return tagMap;
        }
    }
}

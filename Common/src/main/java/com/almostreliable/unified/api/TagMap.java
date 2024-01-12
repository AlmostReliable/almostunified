package com.almostreliable.unified.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.Set;
import java.util.function.Predicate;

public interface TagMap<T> {
    TagMap<T> filtered(Predicate<TagKey<T>> tagFilter, Predicate<ResourceLocation> entryFilter);

    int tagSize();

    int itemSize();

    Set<ResourceLocation> getEntriesByTag(TagKey<T> tag);

    Set<TagKey<T>> getTagsByEntry(ResourceLocation entry);

    Set<TagKey<T>> getTags();
}

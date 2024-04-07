package com.almostreliable.unified.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.Set;

public interface TagMap<T> {

    int tagSize();

    int itemSize();

    Set<UnifyEntry<T>> getEntriesByTag(TagKey<T> tag);

    Set<TagKey<T>> getTagsByEntry(ResourceLocation entry);

    Set<TagKey<T>> getTags();
}

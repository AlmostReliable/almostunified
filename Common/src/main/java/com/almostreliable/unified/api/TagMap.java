package com.almostreliable.unified.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.Set;

public interface TagMap<T> {

    int tagSize();

    int itemSize();

    Set<UnifyEntry<T>> getEntriesByTag(TagKey<T> tag);

    @Nullable
    TagKey<T> getTag(ResourceLocation entry);

    @Nullable
    TagKey<T> getTag(Item item);

    Set<TagKey<T>> getTags();
}

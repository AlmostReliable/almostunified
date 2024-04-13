package com.almostreliable.unified.api;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.Collection;

public interface TagInheritance<T> {

    boolean skipForInheritance(TagKey<Item> unifyEntry);

    boolean shouldInherit(TagKey<T> tag, Collection<TagKey<Item>> tags);
}

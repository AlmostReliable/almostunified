package com.almostreliable.unified.utils;

import com.almostreliable.unified.AlmostUnified;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.*;

public class TagDelegateHelper {

    /**
     * A map of tags to their delegate tags.
     * <p>
     * For example, if the map contains the entry {@code minecraft:logs -> minecraft:planks},
     * then any recipes that use the tag {@code minecraft:logs} will be replaced with the tag
     * {@code minecraft:planks}.
     * <p>
     * Map Key = Tag to replace<br>
     * Map Value = Tag to delegate to
     */
    Map<UnifyTag<Item>, UnifyTag<Item>> delegates = new HashMap<>();
    Multimap<UnifyTag<Item>, UnifyTag<Item>> parentsToChildren = HashMultimap.create();

    public TagDelegateHelper(Map<ResourceLocation, Set<ResourceLocation>> tagDelegates) {
        tagDelegates.forEach((rawDelegate, rawTags) -> {
            rawTags.forEach(rawTag -> {
                UnifyTag<Item> delegate = UnifyTag.item(rawDelegate);
                UnifyTag<Item> tag = UnifyTag.item(rawTag);
                parentsToChildren.put(delegate, tag);
                delegates.put(tag, delegate);
            });
        });
    }

    public void validate(List<UnifyTag<Item>> tags) {
        Set<UnifyTag<Item>> asSet = new HashSet<>(tags);

        for (UnifyTag<Item> parent : parentsToChildren.keySet()) {
            if (!asSet.contains(parent)) {
                AlmostUnified.LOG.warn("Tag delegate {} is not present in the tag list.", parent.location());
            }
        }

        for (UnifyTag<Item> tag : getTagsToDelegate()) {
            if (asSet.contains(tag)) {
                //noinspection ConstantConditions
                AlmostUnified.LOG.warn("Tag {} is present in the tag list, but is also marked to be delegate to tag {}.",
                        tag.location(),
                        getDelegate(tag).location());
            }
        }
    }

    /**
     * @return A collection of tags that are marked to be delegated to another tag.
     */
    public Collection<UnifyTag<Item>> getTagsToDelegate() {
        return delegates.keySet();
    }

    @Nullable
    public UnifyTag<Item> getDelegate(UnifyTag<Item> tag) {
        return delegates.get(tag);
    }


}

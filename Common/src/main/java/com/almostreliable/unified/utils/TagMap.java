package com.almostreliable.unified.utils;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagManager;
import net.minecraft.world.item.Item;

import java.util.*;

public class TagMap {
    private final Map<UnifyTag<Item>, Set<ResourceLocation>> tagsToItems = new HashMap<>();
    private final Map<ResourceLocation, Set<UnifyTag<Item>>> itemsToTags = new HashMap<>();

    protected TagMap() {}

    public static TagMap create(TagManager tagManager) {
        Objects.requireNonNull(tagManager, "Requires a non-null tag manager");

        var tags = tagManager
                .getResult()
                .stream()
                .filter(result -> result.key().equals(Registry.ITEM_REGISTRY))
                .findFirst()
                .map(TagManager.LoadResult::tags)
                .orElseThrow(() -> new IllegalStateException("No item tag result found"));

        TagMap tagMap = new TagMap();

        for (var entry : tags.entrySet()) {
            UnifyTag<Item> tag = UnifyTag.item(entry.getKey());
            Tag<? extends Holder<?>> holderTag = entry.getValue();

            for (Holder<?> holder : holderTag.getValues()) {
                holder
                        .unwrapKey()
                        .map(ResourceKey::location)
                        .filter(Registry.ITEM::containsKey)
                        .ifPresent(itemId -> tagMap.put(tag, itemId));
            }
        }

        return tagMap;
    }

    protected void put(UnifyTag<Item> tag, ResourceLocation item) {
        tagsToItems.computeIfAbsent(tag, k -> new HashSet<>()).add(item);
        itemsToTags.computeIfAbsent(item, k -> new HashSet<>()).add(tag);
    }

    public Collection<ResourceLocation> getItems(UnifyTag<Item> tag) {
        return Collections.unmodifiableSet(tagsToItems.getOrDefault(tag, Collections.emptySet()));
    }

    public Collection<UnifyTag<Item>> getTags(ResourceLocation items) {
        return Collections.unmodifiableSet(itemsToTags.getOrDefault(items, Collections.emptySet()));
    }

    public int tagSize() {
        return tagsToItems.size();
    }

    public int itemSize() {
        return itemsToTags.size();
    }
}

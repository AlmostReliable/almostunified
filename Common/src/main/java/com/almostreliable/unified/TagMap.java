package com.almostreliable.unified;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagManager;

import java.util.*;

public class TagMap {
    private final Map<ResourceLocation, Set<ResourceLocation>> tagsToItems = new HashMap<>();
    private final Map<ResourceLocation, Set<ResourceLocation>> itemsToTags = new HashMap<>();

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
            ResourceLocation tag = entry.getKey();
            Tag<? extends Holder<?>> holderTag = entry.getValue();

            for (Holder<?> holder : holderTag.getValues()) {
                ResourceLocation itemId = holder.unwrapKey().map(ResourceKey::location).orElse(null);
                if (itemId != null) {
                    tagMap.put(tag, itemId);
                }
            }
        }

        return tagMap;
    }

    protected void put(ResourceLocation tag, ResourceLocation item) {
        tagsToItems.computeIfAbsent(tag, k -> new HashSet<>()).add(item);
        itemsToTags.computeIfAbsent(item, k -> new HashSet<>()).add(tag);
    }

    public Collection<ResourceLocation> getItems(ResourceLocation tag) {
        return Collections.unmodifiableSet(tagsToItems.getOrDefault(tag, Collections.emptySet()));
    }

    public Collection<ResourceLocation> getTags(ResourceLocation items) {
        return Collections.unmodifiableSet(itemsToTags.getOrDefault(items, Collections.emptySet()));
    }
}

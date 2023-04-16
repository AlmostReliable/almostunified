package com.almostreliable.unified.utils;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagManager;
import net.minecraft.world.item.Item;

import java.util.*;
import java.util.function.Predicate;

public class TagMap {

    private final Map<UnifyTag<Item>, Set<ResourceLocation>> tagsToItems = new HashMap<>();
    private final Map<ResourceLocation, Set<UnifyTag<Item>>> itemsToTags = new HashMap<>();

    protected TagMap() {}

    public static TagMap create(Collection<UnifyTag<Item>> unifyTags) {
        TagMap tagMap = new TagMap();

        unifyTags.forEach(ut -> {
            TagKey<Item> asTagKey = TagKey.create(Registry.ITEM_REGISTRY, ut.location());
            fill(tagMap, ut, asTagKey);
        });

        return tagMap;
    }

    /**
     * Creates a TagMap from a vanilla {@link TagManager}.
     * <p>
     * Uses a {@link TagOwnerships} to process tag delegates.
     *
     * @param tagManager    The vanilla tag manager.
     * @param tagOwnerships The tag delegate helper.
     * @return A new TagMap.
     */
    public static TagMap create(TagManager tagManager) {
        var tags = unpackTagManager(tagManager);
        TagMap tagMap = new TagMap();

        for (var entry : tags.entrySet()) {
            UnifyTag<Item> unifyTag = UnifyTag.item(entry.getKey());
            for (Holder<?> holder : entry.getValue()) {
                fill(tagMap, unifyTag, holder);
            }
        }

        return tagMap;
    }

    private static void fill(TagMap tagMap, UnifyTag<Item> storageTag, TagKey<Item> holderTag) {
        Registry.ITEM.getTagOrEmpty(holderTag).forEach(holder -> {
            ResourceLocation key = Registry.ITEM.getKey(holder.value());
            tagMap.put(storageTag, key);
        });
    }

    private static Map<ResourceLocation, Collection<Holder<Item>>> unpackTagManager(TagManager tagManager) {
        var tags = tagManager
                .getResult()
                .stream()
                .filter(result -> result.key() == Registry.ITEM_REGISTRY)
                .findFirst()
                .map(TagManager.LoadResult::tags)
                .orElseThrow(() -> new IllegalStateException("No item tag result found"));

        return Utils.cast(tags);
    }

    private static void fill(TagMap tagMap, UnifyTag<Item> unifyTag, Holder<?> holder) {
        holder
                .unwrapKey()
                .map(ResourceKey::location)
                .filter(Registry.ITEM::containsKey)
                .ifPresent(itemId -> tagMap.put(unifyTag, itemId));
    }

    /**
     * Creates a filtered {@link TagMap}.
     *
     * @param tagFilter  A filter to determine which tags to include.
     * @param itemFilter A filter to determine which items to include.
     * @return A new {@link TagMap}.
     */
    public TagMap filtered(Predicate<UnifyTag<Item>> tagFilter, Predicate<ResourceLocation> itemFilter) {
        TagMap tagMap = new TagMap();

        tagsToItems.forEach((tag, items) -> {
            if (!tagFilter.test(tag)) {
                return;
            }
            items.stream().filter(itemFilter).forEach(item -> tagMap.put(tag, item));
        });

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

    public Collection<UnifyTag<Item>> getTags() {
        return Collections.unmodifiableSet(tagsToItems.keySet());
    }
}

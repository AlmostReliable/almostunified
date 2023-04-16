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

    /**
     * Creates a tag map from a set of unify tags.
     * <p>
     * This should only be used for client-side tag maps or for tests.<br>
     * It requires the registry to be loaded in order to validate the tags
     * and fetch the holder from it.
     * <p>
     * For the server, use {@link #create(TagManager)} instead.
     *
     * @param unifyTags The unify tags.
     * @return A new tag map.
     */
    public static TagMap create(Set<UnifyTag<Item>> unifyTags) {
        TagMap tagMap = new TagMap();

        unifyTags.forEach(ut -> {
            TagKey<Item> asTagKey = TagKey.create(Registry.ITEM_REGISTRY, ut.location());
            Registry.ITEM.getTagOrEmpty(asTagKey).forEach(holder -> {
                ResourceLocation key = Registry.ITEM.getKey(holder.value());
                tagMap.put(ut, key);
            });
        });

        return tagMap;
    }

    /**
     * Creates a tag map from the vanilla {@link TagManager}.
     * <p>
     * This should only be used on the server.<br>
     * It will fetch all tags and items from the manager and store them. This tag map should later
     * be filtered by using {@link #filtered(Predicate, Predicate)}.
     * <p>
     * For the client, use {@link #create(Set)} instead.
     *
     * @param tagManager The vanilla tag manager.
     * @return A new tag map.
     */
    public static TagMap create(TagManager tagManager) {
        var tags = unpackTagManager(tagManager);
        TagMap tagMap = new TagMap();

        for (var entry : tags.entrySet()) {
            UnifyTag<Item> unifyTag = UnifyTag.item(entry.getKey());
            for (Holder<?> holder : entry.getValue()) {
                holder
                        .unwrapKey()
                        .map(ResourceKey::location)
                        .filter(Registry.ITEM::containsKey)
                        .ifPresent(itemId -> tagMap.put(unifyTag, itemId));
            }
        }

        return tagMap;
    }

    /**
     * Creates a filtered tag map copy.
     *
     * @param tagFilter  A filter to determine which tags to include.
     * @param itemFilter A filter to determine which items to include.
     * @return A filtered copy of this tag map.
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

    public int tagSize() {
        return tagsToItems.size();
    }

    public int itemSize() {
        return itemsToTags.size();
    }

    public Collection<ResourceLocation> getItemsByTag(UnifyTag<Item> tag) {
        return Collections.unmodifiableSet(tagsToItems.getOrDefault(tag, Collections.emptySet()));
    }

    public Collection<UnifyTag<Item>> getTagsByItem(ResourceLocation items) {
        return Collections.unmodifiableSet(itemsToTags.getOrDefault(items, Collections.emptySet()));
    }

    public Collection<UnifyTag<Item>> getTags() {
        return Collections.unmodifiableSet(tagsToItems.keySet());
    }

    /**
     * Helper function to build a relationship between a tag and an item.
     * <p>
     * If the entries don't exist in the internal maps yet, they will be created. That means
     * it needs to be checked whether the tag or item is valid before calling this method.
     *
     * @param tag  The tag.
     * @param item The item.
     */
    protected void put(UnifyTag<Item> tag, ResourceLocation item) {
        tagsToItems.computeIfAbsent(tag, k -> new HashSet<>()).add(item);
        itemsToTags.computeIfAbsent(item, k -> new HashSet<>()).add(tag);
    }

    /**
     * Helper function to fetch all item tags and their item holders from the tag manager.
     *
     * @param tagManager The tag manager.
     * @return A map of all item tags and their item holders.
     */
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
}

package com.almostreliable.unified.utils;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.api.recipe.ReplacementFallbackStrategy;
import com.almostreliable.unified.recipe.fallbacks.StoneStrataFallbackStrategy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReplacementMap {

    private final Collection<String> modPriorities;
    private final TagMap tagMap;
    private final Map<ResourceLocation, UnifyTag<Item>> itemToTagMapping;
    // TODO - In the future this may be a list of multiple fallbacks.
    private final ReplacementFallbackStrategy fallbackStrategy = new StoneStrataFallbackStrategy();

    public ReplacementMap(TagMap tagMap, List<UnifyTag<Item>> allowedTags, List<String> modPriorities) {
        this.tagMap = tagMap;
        this.modPriorities = modPriorities;
        this.itemToTagMapping = createItemMapping(allowedTags);
    }

    protected Map<ResourceLocation, UnifyTag<Item>> createItemMapping(List<UnifyTag<Item>> allowedTags) {
        Map<ResourceLocation, UnifyTag<Item>> itemToTagMapping = new HashMap<>(allowedTags.size());
        for (UnifyTag<Item> tag : allowedTags) {
            Collection<ResourceLocation> items = tagMap.getItems(tag);
            for (ResourceLocation item : items) {
                if (itemToTagMapping.containsKey(item)) {
                    AlmostUnified.LOG.warn("Item '{}' already has a tag '{}' for recipe replacement. Skipping this tag",
                            item,
                            tag);
                    continue;
                }

                itemToTagMapping.put(item, tag);
            }
        }

        return itemToTagMapping;
    }

    @Nullable
    public UnifyTag<Item> getPreferredTag(ResourceLocation item) {
        return itemToTagMapping.get(item);
    }

    @Nullable
    public ResourceLocation getReplacementForItem(ResourceLocation item) {
        UnifyTag<Item> tag = getPreferredTag(item);
        if (tag == null) {
            return null;
        }

        ResourceLocation preferredItem = getPreferredItemByTag(tag, item.getNamespace());
        if (item.equals(preferredItem)) {
            return null;
        }

        return preferredItem;
    }

    @Nullable
    public ResourceLocation getPreferredItemByTag(UnifyTag<Item> tag) {
        return getPreferredItemByTag(tag, null);
    }

    @Nullable
    public ResourceLocation getPreferredItemByTag(UnifyTag<Item> tag, @Nullable String ignoredNamespace) {
        for (String mod : modPriorities) {
            if(mod.equals(ignoredNamespace)) {
                return null;
            }

            List<ResourceLocation> sameModItems = tagMap
                    .getItems(tag)
                    .stream()
                    .filter(i -> i.getNamespace().equals(mod))
                    .toList();
            if (sameModItems.size() == 1) {
                return sameModItems.get(0);
            }

            if (sameModItems.size() > 1) {
                ResourceLocation fallback = fallbackStrategy.getFallback(tag, sameModItems, tagMap);
                if (fallback != null) {
                    return fallback;
                }
            }
        }

        return null;
    }
}

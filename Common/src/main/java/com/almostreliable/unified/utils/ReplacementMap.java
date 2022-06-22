package com.almostreliable.unified.utils;

import com.almostreliable.unified.api.recipe.ReplacementFallbackStrategy;
import com.almostreliable.unified.recipe.fallbacks.StoneStrataFallbackStrategy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ReplacementMap {

    private final Collection<String> modPriorities;
    private final TagMap tagMap;
    private final Map<ResourceLocation, TagKey<Item>> itemToTagMapping;
    // TODO - In the future this may be a list of multiple fallbacks.
    private final ReplacementFallbackStrategy fallbackStrategy = new StoneStrataFallbackStrategy();

    public ReplacementMap(TagMap tagMap, Map<ResourceLocation, TagKey<Item>> itemToTagMapping, List<String> modPriorities) {
        this.tagMap = tagMap;
        this.itemToTagMapping = itemToTagMapping;
        this.modPriorities = modPriorities;
    }

    @Nullable
    public TagKey<Item> getPreferredTag(ResourceLocation item) {
        return itemToTagMapping.get(item);
    }

    @Nullable
    public ResourceLocation getReplacementForItem(ResourceLocation item) {
        TagKey<Item> tag = getPreferredTag(item);
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
    public ResourceLocation getPreferredItemByTag(TagKey<Item> tag) {
        return getPreferredItemByTag(tag, null);
    }

    @Nullable
    public ResourceLocation getPreferredItemByTag(TagKey<Item> tag, @Nullable String ignoredNamespace) {
        for (String mod : modPriorities) {
            List<ResourceLocation> sameModItems = tagMap
                    .getItems(tag)
                    .stream()
                    .filter(i -> i.getNamespace().equals(mod))
                    .toList();
            if (sameModItems.size() == 1) {
                return sameModItems.get(0);
            }

            if (sameModItems.size() > 1 && !mod.equals(ignoredNamespace)) {
                ResourceLocation fallback = fallbackStrategy.getFallback(tag, sameModItems, tagMap);
                if (fallback != null) {
                    return fallback;
                }
            }
        }

        return null;
    }
}

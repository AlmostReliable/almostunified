package com.almostreliable.unified.utils;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.api.recipe.ReplacementFallbackStrategy;
import com.almostreliable.unified.recipe.fallbacks.StoneStrataFallbackStrategy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public class ReplacementMap {

    private final Collection<String> modPriorities;
    private final TagMap tagMap;
    // TODO - In the future this may be a list of multiple fallbacks.
    private final ReplacementFallbackStrategy fallbackStrategy = new StoneStrataFallbackStrategy();

    public ReplacementMap(TagMap tagMap, List<String> modPriorities) {
        this.tagMap = tagMap;
        this.modPriorities = modPriorities;
    }

    @Nullable
    public UnifyTag<Item> getPreferredTag(ResourceLocation item) {
        Collection<UnifyTag<Item>> tags = tagMap.getTags(item);

        if (tags.isEmpty()) {
            return null;
        }

        if (tags.size() > 1) {
            AlmostUnified.LOG.warn(
                    "Item '{}' has multiple preferred tags '{}' for recipe replacement. This needs to be manually fixed by the user.",
                    item,
                    tags.stream().map(UnifyTag::location).toList());
        }

        return tags.iterator().next();
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
            if (mod.equals(ignoredNamespace)) {
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

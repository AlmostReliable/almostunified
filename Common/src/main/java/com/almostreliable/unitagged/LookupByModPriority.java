package com.almostreliable.unitagged;

import com.almostreliable.unitagged.api.ReplacementLookupHelper;
import com.google.common.collect.Multimap;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class LookupByModPriority implements ReplacementLookupHelper {
    private final Collection<String> modPriorities;
    private final Map<ResourceLocation, ResourceLocation> itemToTag;
    private final Multimap<ResourceLocation, ResourceLocation> tagToItem;

    /**
     * Cache for replacements. Key is the item to replace, value is the replacement.
     */
    private final Map<ResourceLocation, ResourceLocation> replacementCache = new HashMap<>();

    public LookupByModPriority(Map<ResourceLocation, ResourceLocation> itemToTag, Multimap<ResourceLocation, ResourceLocation> tagToItem, Collection<String> modPriorities) {
        this.itemToTag = itemToTag;
        this.tagToItem = tagToItem;
        this.modPriorities = modPriorities;
    }

    @Nullable
    @Override
    public String findReplacement(String id) {
        ResourceLocation asLocation = new ResourceLocation(id);
        if (!Registry.ITEM.containsKey(asLocation)) {
            return null;
        }

        ResourceLocation tagRL = itemToTag.get(asLocation);
        if (tagRL == null) {
            return null;
        }

        ResourceLocation replacement = replacementCache.computeIfAbsent(
                asLocation,
                key -> computeReplacement(key, tagToItem.get(tagRL))
        );
        return replacement.toString();
    }

    private ResourceLocation computeReplacement(ResourceLocation toReplace, Collection<ResourceLocation> items) {
        for (String mod : modPriorities) {
            for (ResourceLocation item : items) {
                if (item.getNamespace().equals(mod) && Registry.ITEM.containsKey(item)) {
                    return item;
                }
            }
        }

        return toReplace;
    }
}

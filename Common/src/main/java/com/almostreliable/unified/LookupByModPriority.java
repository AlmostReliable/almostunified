package com.almostreliable.unified;

import com.almostreliable.unified.api.ReplacementLookupHelper;
import com.google.common.collect.Multimap;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.*;

public class LookupByModPriority implements ReplacementLookupHelper {
    private final Collection<String> modPriorities;
    private final Map<ResourceLocation, ResourceLocation> itemToTag;
    private final Multimap<ResourceLocation, ResourceLocation> tagToItem;

    /**
     * Cache for replacements. Key is the item to replace, value is the replacement.
     */
    private final Map<ResourceLocation, ResourceLocation> replacementCache = new HashMap<>();
    private final Set<ResourceLocation> invalidCache = new HashSet<>();

    public LookupByModPriority(Map<ResourceLocation, ResourceLocation> itemToTag, Multimap<ResourceLocation, ResourceLocation> tagToItem, Collection<String> modPriorities) {
        this.itemToTag = itemToTag;
        this.tagToItem = tagToItem;
        this.modPriorities = modPriorities;
    }

    @Nullable
    @Override
    public String findReplacement(String id) {
        ResourceLocation asLocation = new ResourceLocation(id);
        if(invalidCache.contains(asLocation)) {
            return null;
        }

        ResourceLocation replacement = replacementCache.get(asLocation);
        if(replacement != null) {
            return replacement.toString();
        }

        ResourceLocation tagRL = itemToTag.get(asLocation);
        if (!Registry.ITEM.containsKey(asLocation) || tagRL == null) {
            return null;
        }

        ResourceLocation modReplacement = computeReplacement(asLocation, tagToItem.get(tagRL));
        if(modReplacement == null || modReplacement.equals(asLocation)) {
            invalidCache.add(asLocation);
            return null;
        }

        replacementCache.put(asLocation, modReplacement);
        AlmostUnified.LOG.info("########### {} -> {}", id, modReplacement.toString());
        return modReplacement.toString();
    }

    @Nullable
    private ResourceLocation computeReplacement(ResourceLocation toReplace, Collection<ResourceLocation> items) {
        for (String mod : modPriorities) {
            for (ResourceLocation item : items) {
                if (item.getNamespace().equals(mod) && Registry.ITEM.containsKey(item)) {
                    return item;
                }
            }
        }

        return null;
    }
}

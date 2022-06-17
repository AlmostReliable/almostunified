package com.almostreliable.unified;

import com.almostreliable.unified.api.RecipeTransformContext;
import com.google.common.collect.Multimap;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.*;

public class RecipeTransformContextImpl implements RecipeTransformContext {

    private final Collection<String> modPriorities;
    private final TagMap tagMap;
    private final Map<ResourceLocation, ResourceLocation> itemToTagMapping;
    /**
     * Cache for replacements. Key is the item to replace, value is the replacement.
     */
    private final Map<ResourceLocation, ResourceLocation> replacementCache = new HashMap<>();
    private final Set<ResourceLocation> invalidCache = new HashSet<>();


    public RecipeTransformContextImpl(TagMap tagMap, Map<ResourceLocation, ResourceLocation> itemToTagMapping, List<String> modPriorities) {
        this.tagMap = tagMap;
        this.itemToTagMapping = itemToTagMapping;
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

        ResourceLocation tag = itemToTagMapping.get(asLocation);
        if (!Registry.ITEM.containsKey(asLocation) || tag == null) {
            return null;
        }

        ResourceLocation modReplacement = computeReplacement(asLocation, tag, tagMap.getItems(tag));
        if(modReplacement == null || modReplacement.equals(asLocation)) {
            invalidCache.add(asLocation);
            return null;
        }

        replacementCache.put(asLocation, modReplacement);
        AlmostUnified.LOG.info("########### {} -> {}", id, modReplacement.toString());
        return modReplacement.toString();
    }

    @Nullable
    public ResourceLocation computeReplacement(ResourceLocation toReplace, ResourceLocation tag, Collection<ResourceLocation> items) {
        for (String mod : modPriorities) {
            List<ResourceLocation> sameModItems = tagMap
                    .getItems(tag)
                    .stream()
                    .filter(item -> item.getNamespace().equals(mod) && Registry.ITEM.containsKey(item))
                    .toList();
            if(sameModItems.size() == 1) {
                return sameModItems.get(0);
            }

            if(sameModItems.size() > 1) {
                return findFittingItem(toReplace, tag, sameModItems);
            }
        }

        return null;
    }

    public ResourceLocation findFittingItem(ResourceLocation toReplace, ResourceLocation tag, List<ResourceLocation> sameModItems) {
        return null;
    }
}

package com.almostreliable.unified;

import com.almostreliable.unified.api.RecipeTransformContext;
import com.almostreliable.unified.api.ReplacementFallbackStrategy;
import com.almostreliable.unified.fallbacks.StoneStrataFallbackStrategy;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.*;

public class RecipeTransformContextImpl implements RecipeTransformContext {

    private final Collection<String> modPriorities;
    private final TagMap tagMap;
    private final Map<ResourceLocation, TagKey<Item>> itemToTagMapping;
    /**
     * Cache for replacements. Key is the item to replace, value is the replacement.
     */
    private final Map<ResourceLocation, ResourceLocation> replacementCache = new HashMap<>();
    private final Set<ResourceLocation> invalidCache = new HashSet<>();
    // TODO - In the future this may be a list of multiple fallbacks.
    private final ReplacementFallbackStrategy fallbackStrategy = new StoneStrataFallbackStrategy();


    public RecipeTransformContextImpl(TagMap tagMap, Map<ResourceLocation, TagKey<Item>> itemToTagMapping, List<String> modPriorities) {
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

        TagKey<Item> tag = itemToTagMapping.get(asLocation);
        if (!Registry.ITEM.containsKey(asLocation) || tag == null) {
            return null;
        }

        ResourceLocation modReplacement = computeReplacement(asLocation, tag);
        if(modReplacement == null || modReplacement.equals(asLocation)) {
            invalidCache.add(asLocation);
            return null;
        }

        replacementCache.put(asLocation, modReplacement);
        AlmostUnified.LOG.info("########### {} -> {}", id, modReplacement.toString());
        return modReplacement.toString();
    }

    @Nullable
    public ResourceLocation computeReplacement(ResourceLocation item, TagKey<Item> tag) {
        for (String mod : modPriorities) {
            if(mod.equals(item.getNamespace())) {
                return null;
            }

            List<ResourceLocation> sameModItems = tagMap
                    .getItems(tag)
                    .stream()
                    .filter(i -> i.getNamespace().equals(mod) && Registry.ITEM.containsKey(i))
                    .toList();
            if(sameModItems.size() == 1) {
                return sameModItems.get(0);
            }

            if(sameModItems.size() > 1) {
                ResourceLocation fallback = fallbackStrategy.getFallback(item, tag, sameModItems, tagMap);
                if(fallback != null) {
                    if(fallback.equals(item)) {
                        throw new IllegalStateException("Fallback for " + item + " is the same as the item itself. This is not allowed.");
                    }
                    return fallback;
                }
            }

        }

        return null;
    }
}

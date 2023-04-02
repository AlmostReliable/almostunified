package com.almostreliable.unified.compat;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.AlmostUnifiedRuntime;
import com.almostreliable.unified.utils.ReplacementMap;
import com.almostreliable.unified.utils.TagMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class HideHelper {

    public static Collection<ItemStack> createHidingList(AlmostUnifiedRuntime runtime) {
        ReplacementMap repMap = runtime.getReplacementMap().orElse(null);
        TagMap filteredTagMap = runtime.getFilteredTagMap().orElse(null);

        if (repMap == null || filteredTagMap == null) return new ArrayList<>();

        return filteredTagMap.getTags().stream().map(unifyTag -> {
            Collection<ResourceLocation> itemsByTag = filteredTagMap.getItems(unifyTag);

            // avoid hiding single entries and tags that only contain the same namespace for all items
            long namespaces = itemsByTag.stream().map(ResourceLocation::getNamespace).distinct().count();
            if (namespaces <= 1) return new ArrayList<ItemStack>();

            Set<ResourceLocation> replacements = itemsByTag
                    .stream()
                    .map(item -> getReplacementForItem(repMap, item))
                    .collect(Collectors.toSet());
            List<ResourceLocation> toHide = itemsByTag.stream().filter(rl -> !replacements.contains(rl)).toList();

            if (!toHide.isEmpty()) {
                AlmostUnified.LOG.info("Hiding {}/{} items for tag {} -> {}",
                        toHide.size(),
                        itemsByTag.size(),
                        unifyTag.location(),
                        toHide);
            }

            return toHide.stream().flatMap(rl -> BuiltInRegistries.ITEM.getOptional(rl).stream()).map(ItemStack::new).toList();
        }).flatMap(Collection::stream).toList();
    }

    /**
     * Returns the replacement for the given item, or the item itself if no replacement is found.
     * <p>
     * Returning the item itself is important for stone strata detection.
     *
     * @param repMap The replacement map.
     * @param item   The item to get the replacement for.
     * @return The replacement for the given item, or the item itself if no replacement is found.
     */
    private static ResourceLocation getReplacementForItem(ReplacementMap repMap, ResourceLocation item) {
        var replacement = repMap.getReplacementForItem(item);
        if (replacement == null) return item;
        return replacement;
    }
}

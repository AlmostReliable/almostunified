package com.almostreliable.unified.impl;

import com.almostreliable.unified.AlmostUnifiedPlatform;
import com.almostreliable.unified.api.StoneStrataLookup;
import com.almostreliable.unified.utils.VanillaTagWrapper;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.*;
import java.util.regex.Pattern;

public final class StoneStrataLookupImpl implements StoneStrataLookup {

    private final List<String> stoneStrata;
    private final Pattern tagMatcher;
    private final Map<ResourceLocation, String> itemToStoneStrata;

    // don't clear the caches, so they are available for the runtime and KubeJS binding
    // the runtime holding this handler is automatically yeeted on reload
    private final Map<TagKey<Item>, Boolean> stoneStrataTagCache;
    private final Map<ResourceLocation, String> stoneStrataCache;

    private StoneStrataLookupImpl(Collection<String> stoneStrata, Pattern tagMatcher, Map<ResourceLocation, String> itemToStoneStrata) {
        this.stoneStrata = createSortedStoneStrata(stoneStrata);
        this.tagMatcher = tagMatcher;
        this.itemToStoneStrata = itemToStoneStrata;
        this.stoneStrataTagCache = new HashMap<>();
        this.stoneStrataCache = new HashMap<>();
    }

    /**
     * Returns the stone strata list sorted from longest to shortest.
     * <p>
     * This is required to ensure that the longest strata is returned and no sub-matches happen.<br>
     * Example: "nether" and "blue_nether" would both match "nether" if the list is not sorted.
     *
     * @param stoneStrata The stone strata list to sort.
     * @return The sorted stone strata list.
     */
    private static List<String> createSortedStoneStrata(Collection<String> stoneStrata) {
        return stoneStrata.stream().sorted(Comparator.comparingInt(String::length).reversed()).toList();
    }

    public static StoneStrataLookup create(Collection<String> stoneStrataIds, VanillaTagWrapper<Item> tags) {
        var stoneStrataTags = AlmostUnifiedPlatform.INSTANCE.getStoneStrataTags(stoneStrataIds);

        Map<ResourceLocation, TagKey<Item>> itemToTag = new HashMap<>();
        for (TagKey<Item> sst : stoneStrataTags) {
            for (Holder<Item> holder : tags.get(sst)) {
                ResourceLocation itemId = holder
                        .unwrapKey()
                        .orElseThrow(() -> new IllegalStateException("Tag is not bound for holder " + holder))
                        .location();
                TagKey<Item> old = itemToTag.put(itemId, sst);
                if (old != null) {
                    throw new IllegalStateException(
                            "Item " + itemId + " is bound to multiple tags: " + old + " and " + sst);
                }
            }
        }

        Map<ResourceLocation, String> itemToStoneStrata = new HashMap<>();
        itemToTag.forEach((item, tag) -> {
            // TODO custom handler somehow?
            String tagStr = tag.location().toString();
            int i = tagStr.lastIndexOf('/'); // forge:ores_in_ground/stone -> stone
            if (i > 0) {
                itemToStoneStrata.put(item, tagStr.substring(i + 1));
            }
        });

        Pattern tagMatcher = Pattern.compile(switch (AlmostUnifiedPlatform.INSTANCE.getPlatform()) {
            case NEO_FORGE -> "forge:ores/.+";
            case FABRIC -> "(c:ores/.+|(minecraft|c):.+_ores)";
        });
        return new StoneStrataLookupImpl(stoneStrataIds, tagMatcher, itemToStoneStrata);
    }


    @Override
    public String getStoneStrata(ResourceLocation item) {
        return stoneStrataCache.computeIfAbsent(item, this::computeStoneStrata);
    }

    /**
     * Implementation logic for {@link #getStoneStrata(ResourceLocation)}.
     *
     * @param item The item to get the stone strata from.
     * @return The stone strata of the item. Clean stone strata returns an empty string for later sorting as a
     * fallback variant.
     */
    private String computeStoneStrata(ResourceLocation item) {
        String strata = itemToStoneStrata.get(item);
        if (strata != null) {
            if (strata.equals("stone")) {
                return "";
            }

            return strata;
        }

        for (String stone : stoneStrata) {
            if (item.getPath().contains(stone + "_")) {
                if (stone.equals("stone")) {
                    return "";
                }
                return stone;
            }
        }

        return "";
    }

    @Override
    public boolean isStoneStrataTag(TagKey<Item> tag) {
        return stoneStrataTagCache.computeIfAbsent(tag, t -> tagMatcher.matcher(t.location().toString()).matches());
    }
}

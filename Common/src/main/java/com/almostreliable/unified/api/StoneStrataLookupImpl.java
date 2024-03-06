package com.almostreliable.unified.api;

import com.almostreliable.unified.AlmostUnifiedPlatform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.*;
import java.util.regex.Pattern;

public final class StoneStrataLookupImpl implements StoneStrataLookup {

    private final List<String> stoneStrata;
    private final Pattern tagMatcher;
    private final TagMap<Item> stoneStrataTagMap;

    // don't clear the caches, so they are available for the runtime and KubeJS binding
    // the runtime holding this handler is automatically yeeted on reload
    private final Map<TagKey<Item>, Boolean> stoneStrataTagCache;
    private final Map<ResourceLocation, String> stoneStrataCache;

    private StoneStrataLookupImpl(Collection<String> stoneStrata, Pattern tagMatcher, TagMap<Item> stoneStrataTagMap) {
        this.stoneStrata = createSortedStoneStrata(stoneStrata);
        this.tagMatcher = tagMatcher;
        this.stoneStrataTagMap = stoneStrataTagMap;
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

    public static StoneStrataLookup create(Collection<String> stoneStrataIds, Set<TagKey<Item>> stoneStrataTags, TagMap<Item> tagMap) {
        var stoneStrataTagMap = tagMap.filtered(stoneStrataTags::contains, item -> true);
        Pattern tagMatcher = Pattern.compile(switch (AlmostUnifiedPlatform.INSTANCE.getPlatform()) {
            case FORGE -> "forge:ores/.+";
            case FABRIC -> "(c:ores/.+|c:.+_ores)";
        });
        return new StoneStrataLookupImpl(stoneStrataIds, tagMatcher, stoneStrataTagMap);
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
        String strata = stoneStrataTagMap
                .getTagsByEntry(item)
                .stream()
                .findFirst()
                .map(TagKey::location)
                .map(ResourceLocation::toString)
                .map(s -> {
                    int i = s.lastIndexOf('/');
                    return i == -1 ? null : s.substring(i + 1);
                })
                .orElse(null);

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

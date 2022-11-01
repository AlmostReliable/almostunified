package com.almostreliable.unified.api;

import com.almostreliable.unified.AlmostUnifiedPlatform;
import com.almostreliable.unified.utils.TagMap;
import com.almostreliable.unified.utils.UnifyTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class StoneStrataHandler {

    private final List<String> stoneStrata;
    private final Pattern tagMatcher;
    private final TagMap stoneStrataTagMap;

    private StoneStrataHandler(List<String> stoneStrata, Pattern tagMatcher, TagMap stoneStrataTagMap) {
        this.stoneStrata = stoneStrata;
        this.tagMatcher = tagMatcher;
        this.stoneStrataTagMap = stoneStrataTagMap;
    }

    public static StoneStrataHandler create(List<String> stoneStrataIds, Set<UnifyTag<Item>> stoneStrataTags, TagMap tagMap) {
        TagMap stoneStrataTagMap = tagMap.filtered(stoneStrataTags::contains, item -> true);
        Pattern tagMatcher = switch (AlmostUnifiedPlatform.INSTANCE.getPlatform()) {
            case FORGE -> Pattern.compile("forge:ores/.+");
            case FABRIC -> Pattern.compile("(c:ores/.+|c:.+_ore)");
        };
        return new StoneStrataHandler(stoneStrataIds, tagMatcher, stoneStrataTagMap);
    }

    /**
     * Returns the stone strata from given item. Method works on the requirement that it's an item which has a stone strata.
     * Use {@link #isStoneStrataTag(UnifyTag)} to fill this requirement.
     *
     * @param item The item to get the stone strata from.
     * @return The stone strata of the item. Returning empty string means clean-stone strata.
     */
    public String getStoneStrata(ResourceLocation item) {
        String strata = stoneStrataTagMap
                .getTags(item)
                .stream()
                .findFirst()
                .map(UnifyTag::location)
                .map(ResourceLocation::toString)
                .map(s -> {
                    int i = s.lastIndexOf('/');
                    return i == -1 ? null : s.substring(i + 1);
                })
                .orElse(null);
        if (strata != null) {
            return strata;
        }

        for (String stone : stoneStrata) {
            if (item.getPath().contains(stone + "_")) {
                return stone;
            }
        }

        return "";
    }

    public boolean isStoneStrataTag(UnifyTag<Item> tag) {
        return tagMatcher.matcher(tag.location().toString()).matches();
    }
}

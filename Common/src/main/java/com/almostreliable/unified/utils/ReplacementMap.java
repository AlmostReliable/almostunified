package com.almostreliable.unified.utils;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.AlmostUnifiedPlatform;
import com.almostreliable.unified.config.UnifyConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ReplacementMap {
    private static final Pattern FABRIC_STRATA_PATTERN = Pattern.compile("(c:ores/.+|c:.+_ore)");

    private final TagMap tagMap;
    private final UnifyConfig unifyConfig;

    public ReplacementMap(TagMap tagMap, UnifyConfig unifyConfig) {
        this.tagMap = tagMap;
        this.unifyConfig = unifyConfig;
    }

    /**
     * Returns the stone strata from given item. Method works on the requirement that it's an item which has a stone strata.
     * Use {@link #isStoneStrataTag(UnifyTag)} to fill this requirement.
     *
     * @param item The item to get the stone strata from.
     * @return The stone strata of the item. Returning empty string means clean-stone strata.
     */
    private String getStoneStrata(ResourceLocation item) {
        for (String stone : unifyConfig.getStoneStrata()) {
            if (item.getPath().contains(stone + "_")) {
                return stone;
            }
        }

        return "";
    }

    private boolean isStoneStrataTag(UnifyTag<Item> tag) {
        String tagString = tag.location().toString();
        return switch (AlmostUnifiedPlatform.INSTANCE.getPlatform()) {
            case FORGE -> tagString.startsWith("forge:ores/");
            case FABRIC -> FABRIC_STRATA_PATTERN.matcher(tagString).matches();
        };
    }

    @Nullable
    public UnifyTag<Item> getPreferredTagForItem(ResourceLocation item) {
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
        UnifyTag<Item> t = getPreferredTagForItem(item);
        if (t == null) {
            return null;
        }

        if (isStoneStrataTag(t)) {
            String stone = getStoneStrata(item);
            return getPreferredItemForTag(t, i -> stone.equals(getStoneStrata(i)));
        }

        return getPreferredItemForTag(t, i -> true);
    }

    @Nullable
    public ResourceLocation getPreferredItemForTag(UnifyTag<Item> tag, Predicate<ResourceLocation> itemFilter) {
        List<ResourceLocation> items = tagMap
                .getItems(tag)
                .stream()
                .filter(itemFilter)
                // Helps us to get the clean stone variant first in case of a stone strata tag
                .sorted(Comparator.comparingInt(value -> value.toString().length()))
                .toList();

        for (String modPriority : unifyConfig.getModPriorities()) {
            for (ResourceLocation item : items) {
                if (item.getNamespace().equals(modPriority)) {
                    return item;
                }
            }
        }

        return null;
    }
}

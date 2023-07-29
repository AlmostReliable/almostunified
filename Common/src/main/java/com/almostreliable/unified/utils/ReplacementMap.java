package com.almostreliable.unified.utils;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.api.StoneStrataHandler;
import com.almostreliable.unified.config.UnifyConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public class ReplacementMap {

    private final UnifyConfig unifyConfig;
    private final TagMap tagMap;
    private final StoneStrataHandler stoneStrataHandler;
    private final TagOwnerships tagOwnerships;
    private final Set<ResourceLocation> warnings;

    public ReplacementMap(UnifyConfig unifyConfig, TagMap tagMap, StoneStrataHandler stoneStrataHandler, TagOwnerships tagOwnerships) {
        this.tagMap = tagMap;
        this.unifyConfig = unifyConfig;
        this.stoneStrataHandler = stoneStrataHandler;
        this.tagOwnerships = tagOwnerships;
        this.warnings = new HashSet<>();
    }

    @Nullable
    public UnifyTag<Item> getPreferredTagForItem(ResourceLocation item) {
        Collection<UnifyTag<Item>> tags = tagMap.getTagsByItem(item);

        if (tags.isEmpty()) {
            return null;
        }

        if (tags.size() > 1 && !warnings.contains(item)) {
            AlmostUnified.LOG.warn(
                    "Item '{}' has multiple preferred tags '{}' for recipe replacement. This needs to be manually fixed by the user.",
                    item,
                    tags.stream().map(UnifyTag::location).toList()
            );
            warnings.add(item);
        }

        return tags.iterator().next();
    }

    @Nullable
    public ResourceLocation getReplacementForItem(ResourceLocation item) {
        UnifyTag<Item> t = getPreferredTagForItem(item);
        if (t == null) {
            return null;
        }

        if (stoneStrataHandler.isStoneStrataTag(t)) {
            String stone = stoneStrataHandler.getStoneStrata(item);
            return getPreferredItemForTag(t, i -> stone.equals(stoneStrataHandler.getStoneStrata(i)));
        }

        return getPreferredItemForTag(t, i -> true);
    }

    @Nullable
    public ResourceLocation getPreferredItemForTag(UnifyTag<Item> tag, Predicate<ResourceLocation> itemFilter) {
        var tagToLookup = tagOwnerships.getOwnerByTag(tag);
        if (tagToLookup == null) tagToLookup = tag;

        List<ResourceLocation> items = tagMap
                .getItemsByTag(tagToLookup)
                .stream()
                .filter(itemFilter)
                // Helps us to get the clean stone variant first in case of a stone strata tag
                .sorted(Comparator.comparingInt(value -> value.toString().length()))
                .toList();

        if (items.isEmpty()) return null;

        ResourceLocation overrideItem = getOverrideForTag(tagToLookup, items);
        if (overrideItem != null) {
            return overrideItem;
        }

        for (String modPriority : unifyConfig.getModPriorities()) {
            ResourceLocation item = findItemByNamespace(items, modPriority);
            if (item != null) return item;
        }

        return null;
    }

    /**
     * Gets all unify tags of the items within the given ingredient and checks
     * whether the given item is in one of those tags.
     *
     * @param ingred The ingredient to get the unify tags from.
     * @param item   The item to check.
     * @return Whether the item is in one of the unify tags of the ingredient.
     */
    public boolean isItemInUnifiedIngredient(Ingredient ingred, ItemStack item) {
        Set<UnifyTag<Item>> checkedTags = new HashSet<>();

        for (ItemStack ingredItem : ingred.getItems()) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(ingredItem.getItem());

            var preferredTag = getPreferredTagForItem(itemId);
            if (preferredTag == null || checkedTags.contains(preferredTag)) continue;
            checkedTags.add(preferredTag);

            var preferredTagKey = TagKey.create(Registries.ITEM, preferredTag.location());
            if (item.is(preferredTagKey)) {
                return true;
            }
        }

        return false;
    }

    @Nullable
    private ResourceLocation getOverrideForTag(UnifyTag<Item> tag, List<ResourceLocation> items) {
        String priorityOverride = unifyConfig.getPriorityOverrides().get(tag.location());
        if (priorityOverride != null) {
            ResourceLocation item = findItemByNamespace(items, priorityOverride);
            if (item != null) return item;
            AlmostUnified.LOG.warn(
                    "Priority override mod '{}' for tag '{}' does not contain a valid item. Falling back to default priority.",
                    priorityOverride,
                    tag.location());
        }
        return null;
    }

    @Nullable
    private ResourceLocation findItemByNamespace(List<ResourceLocation> items, String namespace) {
        for (ResourceLocation item : items) {
            if (item.getNamespace().equals(namespace)) {
                return item;
            }
        }
        return null;
    }

    public TagOwnerships getTagOwnerships() {
        return tagOwnerships;
    }
}

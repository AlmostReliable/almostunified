package com.almostreliable.unified.utils;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.api.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class ReplacementMapImpl implements ReplacementMap {

    private final ModPriorities modPriorities;
    private final TagMap<Item> tagMap;
    private final StoneStrataLookup stoneStrataLookup;
    private final TagOwnerships tagOwnerships;
    private final Set<ResourceLocation> warnings;

    public ReplacementMapImpl(ModPriorities modPriorities, TagMap<Item> tagMap, StoneStrataLookup stoneStrataLookup, TagOwnerships tagOwnerships) {
        this.tagMap = tagMap;
        this.modPriorities = modPriorities;
        this.stoneStrataLookup = stoneStrataLookup;
        this.tagOwnerships = tagOwnerships;
        this.warnings = new HashSet<>();
    }

    @Nullable
    @Override
    public TagKey<Item> getPreferredTagForItem(ResourceLocation item) {
        var tags = tagMap.getTagsByEntry(item);

        if (tags.isEmpty()) {
            return null;
        }

        if (tags.size() > 1 && !warnings.contains(item)) {
            AlmostUnified.LOG.warn(
                    "Item '{}' has multiple preferred tags '{}' for recipe replacement. This needs to be manually fixed by the user.",
                    item,
                    tags.stream().map(TagKey::location).toList()
            );
            warnings.add(item);
        }

        return tags.iterator().next();
    }

    @Nullable
    @Override
    public ResourceLocation getReplacementForItem(ResourceLocation item) {
        var t = getPreferredTagForItem(item);
        if (t == null) {
            return null;
        }

        if (stoneStrataLookup.isStoneStrataTag(t)) {
            String stone = stoneStrataLookup.getStoneStrata(item);
            return getPreferredItemForTag(t, i -> stone.equals(stoneStrataLookup.getStoneStrata(i)));
        }

        return getPreferredItemForTag(t);
    }

    @Nullable
    @Override
    public ResourceLocation getPreferredItemForTag(TagKey<Item> tag) {
        return getPreferredItemForTag(tag, i -> true);
    }

    @Nullable
    @Override
    public ResourceLocation getPreferredItemForTag(TagKey<Item> tag, Predicate<ResourceLocation> itemFilter) {
        var tagToLookup = tagOwnerships.getOwner(tag);
        if (tagToLookup == null) tagToLookup = tag;

        // TODO do we really need a filter way? Maybe have two methods to explicitly check for stone strata
        List<ResourceLocation> items = tagMap
                .getEntriesByTag(tagToLookup)
                .stream()
                .filter(itemFilter)
                // Helps us to get the clean stone variant first in case of a stone strata tag
                .sorted(Comparator.comparingInt(value -> value.toString().length()))
                .toList();

        if (items.isEmpty()) return null;

        return modPriorities.findPreferredItemId(tagToLookup, items);
    }

    /**
     * Gets all unify tags of the items within the given ingredient and checks
     * whether the given item is in one of those tags.
     *
     * @param ingred The ingredient to get the unify tags from.
     * @param item   The item to check.
     * @return Whether the item is in one of the unify tags of the ingredient.
     */
    @Override
    public boolean isItemInUnifiedIngredient(Ingredient ingred, ItemStack item) {
        Set<TagKey<Item>> checkedTags = new HashSet<>();

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

    @Override
    public TagOwnerships getTagOwnerships() {
        return tagOwnerships;
    }
}

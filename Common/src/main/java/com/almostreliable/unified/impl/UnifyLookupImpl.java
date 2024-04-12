package com.almostreliable.unified.impl;

import com.almostreliable.unified.api.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class UnifyLookupImpl implements UnifyLookup {

    private final ModPriorities modPriorities;
    private final TagMap<Item> tagMap;
    private final StoneStrataLookup stoneStrataLookup;
    private final TagOwnerships tagOwnerships;

    public UnifyLookupImpl(ModPriorities modPriorities, TagMap<Item> tagMap, StoneStrataLookup stoneStrataLookup, TagOwnerships tagOwnerships) {
        this.tagMap = tagMap;
        this.modPriorities = modPriorities;
        this.stoneStrataLookup = stoneStrataLookup;
        this.tagOwnerships = tagOwnerships;
    }

    @Nullable
    @Override
    public TagKey<Item> getPreferredTagForItem(ResourceLocation item) {
        UnifyEntry<Item> entry = tagMap.getEntry(item);
        if (entry == null) {
            return null;
        }

        return entry.tag();
    }

    @Nullable
    @Override
    public TagKey<Item> getPreferredTagForItem(Item item) {
        return getPreferredTagForItem(BuiltInRegistries.ITEM.getKey(item));
    }

    @Nullable
    @Override
    public TagKey<Item> getPreferredTagForItem(Holder<Item> item) {
        return getPreferredTagForItem(item.value());
    }

    @Nullable
    @Override
    public UnifyEntry<Item> getReplacementForItem(ResourceLocation item) {
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
    public UnifyEntry<Item> getReplacementForItem(Item item) {
        return getReplacementForItem(BuiltInRegistries.ITEM.getKey(item));
    }

    @Nullable
    @Override
    public UnifyEntry<Item> getReplacementForItem(Holder<Item> item) {
        return getReplacementForItem(BuiltInRegistries.ITEM.getKey(item.value()));
    }

    @Nullable
    @Override
    public UnifyEntry<Item> getPreferredItemForTag(TagKey<Item> tag) {
        return getPreferredItemForTag(tag, i -> true);
    }

    @Nullable
    @Override
    public UnifyEntry<Item> getPreferredItemForTag(TagKey<Item> tag, Predicate<ResourceLocation> itemFilter) {
        var tagToLookup = tagOwnerships.getOwner(tag);
        if (tagToLookup == null) tagToLookup = tag;

        // TODO do we really need a filter way? Maybe have two methods to explicitly check for stone strata
        var items = tagMap
                .getEntriesByTag(tagToLookup)
                .stream()
                .filter(entry -> itemFilter.test(entry.id()))
                // Helps us to get the clean stone variant first in case of a stone strata tag
                .sorted(Comparator.comparingInt(value -> value.id().toString().length()))
                .toList();

        if (items.isEmpty()) return null;

        return modPriorities.findPreferredEntry(tagToLookup, items);
    }

    @Override
    public boolean isItemInUnifiedIngredient(Ingredient ingred, ItemStack item) {
        Set<TagKey<Item>> checkedTags = new HashSet<>();

        for (ItemStack ingredItem : ingred.getItems()) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(ingredItem.getItem());

            var preferredTag = getPreferredTagForItem(itemId);
            if (preferredTag == null || checkedTags.contains(preferredTag)) continue;
            checkedTags.add(preferredTag);

            if (item.is(preferredTag)) {
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

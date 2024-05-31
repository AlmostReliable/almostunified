package com.almostreliable.unified.impl;

import com.almostreliable.unified.api.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public class UnifyLookupImpl implements UnifyLookup {

    private final ModPriorities modPriorities;
    private final StoneStrataLookup stoneStrataLookup;
    private final TagOwnerships tagOwnerships;
    private final Map<TagKey<Item>, Set<UnifyEntry<Item>>> entries;
    private final Map<ResourceLocation, UnifyEntry<Item>> idEntriesToTag;

    private UnifyLookupImpl(ModPriorities modPriorities, Map<TagKey<Item>, Set<UnifyEntry<Item>>> entries, Map<ResourceLocation, UnifyEntry<Item>> idEntriesToTag, StoneStrataLookup stoneStrataLookup, TagOwnerships tagOwnerships) {
        this.entries = entries;
        this.idEntriesToTag = idEntriesToTag;
        this.modPriorities = modPriorities;
        this.stoneStrataLookup = stoneStrataLookup;
        this.tagOwnerships = tagOwnerships;
    }

    @Override
    public Collection<TagKey<Item>> getUnifiedTags() {
        return entries.keySet();
    }

    @Override
    public Collection<UnifyEntry<Item>> getEntries(TagKey<Item> tag) {
        return entries.getOrDefault(tag, Collections.emptySet());
    }

    @Nullable
    @Override
    public UnifyEntry<Item> getEntry(ResourceLocation entry) {
        return idEntriesToTag.get(entry);
    }

    @Nullable
    @Override
    public UnifyEntry<Item> getEntry(Item item) {
        return getEntry(BuiltInRegistries.ITEM.getKey(item));
    }

    @Nullable
    @Override
    public TagKey<Item> getPreferredTagForItem(ResourceLocation item) {
        UnifyEntry<Item> entry = idEntriesToTag.get(item);
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
            return getPreferredEntryForTag(t, i -> stone.equals(stoneStrataLookup.getStoneStrata(i)));
        }

        return getPreferredEntryForTag(t);
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
    public UnifyEntry<Item> getPreferredEntryForTag(TagKey<Item> tag) {
        return getPreferredEntryForTag(tag, i -> true);
    }

    @Nullable
    @Override
    public UnifyEntry<Item> getPreferredEntryForTag(TagKey<Item> tag, Predicate<ResourceLocation> itemFilter) {
        var tagToLookup = tagOwnerships.getOwner(tag);
        if (tagToLookup == null) tagToLookup = tag;

        // TODO do we really need a filter way? Maybe have two methods to explicitly check for stone strata
        var items = getEntries(tagToLookup)
                .stream()
                .filter(entry -> itemFilter.test(entry.id()))
                // Helps us to get the clean stone variant first in case of a stone strata tag
                .sorted(Comparator.comparingInt(value -> value.id().toString().length()))
                .toList();

        if (items.isEmpty()) return null;

        return modPriorities.findPreferredEntry(tagToLookup, items);
    }

    @Override
    public boolean isItemInUnifiedIngredient(Ingredient ingredient, ItemStack item) {
        Set<TagKey<Item>> checkedTags = new HashSet<>();

        for (ItemStack ingredItem : ingredient.getItems()) {
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


    public static class Builder {
        private final Map<TagKey<Item>, Set<UnifyEntry<Item>>> entries = new HashMap<>();
        private final Set<UnifyEntry<Item>> createdEntries = new HashSet<>();

        private void put(TagKey<Item> tag, UnifyEntry<Item> entry) {
            if (createdEntries.contains(entry)) {
                throw new IllegalStateException("Entry already created: " + entry);
            }

            createdEntries.add(entry);
            this.entries.computeIfAbsent(tag, k -> new HashSet<>()).add(entry);
        }

        public Builder put(TagKey<Item> tag, ResourceLocation... entries) {
            for (var entry : entries) {
                UnifyEntryImpl<Item> e = new UnifyEntryImpl<>(BuiltInRegistries.ITEM, entry);
                put(tag, e);
            }

            return this;
        }

        public Builder put(TagKey<Item> tag, Item... entries) {
            for (var entry : entries) {
                UnifyEntryImpl<Item> e = new UnifyEntryImpl<>(BuiltInRegistries.ITEM, entry);
                put(tag, e);
            }

            return this;
        }

        public UnifyLookup build(ModPriorities modPriorities, StoneStrataLookup stoneStrataLookup, TagOwnerships tagOwnerships) {
            ImmutableMap.Builder<TagKey<Item>, Set<UnifyEntry<Item>>> entries = ImmutableMap.builder();
            ImmutableMap.Builder<ResourceLocation, UnifyEntry<Item>> idEntriesToTag = ImmutableMap.builder();
            this.entries.forEach((t, e) -> {
                ImmutableSet.Builder<UnifyEntry<Item>> set = ImmutableSet.builder();
                for (UnifyEntry<Item> entry : e) {
                    set.add(entry);
                    ((UnifyEntryImpl<Item>) entry).bindTag(t);
                    idEntriesToTag.put(entry.id(), entry);
                }

                entries.put(t, set.build());
            });

            var map = entries.build();
            return new UnifyLookupImpl(modPriorities, map, idEntriesToTag.build(), stoneStrataLookup, tagOwnerships);
        }
    }
}

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
    private final StoneVariantLookup stoneVariantLookup;
    private final TagSubstitutions tagSubstitutions;
    private final Map<TagKey<Item>, Set<UnifyEntry<Item>>> entries;
    private final Map<ResourceLocation, UnifyEntry<Item>> idEntriesToTag;

    private UnifyLookupImpl(ModPriorities modPriorities, Map<TagKey<Item>, Set<UnifyEntry<Item>>> entries, Map<ResourceLocation, UnifyEntry<Item>> idEntriesToTag, StoneVariantLookup stoneVariantLookup, TagSubstitutions tagSubstitutions) {
        this.entries = entries;
        this.idEntriesToTag = idEntriesToTag;
        this.modPriorities = modPriorities;
        this.stoneVariantLookup = stoneVariantLookup;
        this.tagSubstitutions = tagSubstitutions;
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
    public TagKey<Item> getRelevantItemTag(ResourceLocation item) {
        UnifyEntry<Item> entry = idEntriesToTag.get(item);
        if (entry == null) {
            return null;
        }

        return entry.tag();
    }

    @Nullable
    @Override
    public TagKey<Item> getRelevantItemTag(Item item) {
        return getRelevantItemTag(BuiltInRegistries.ITEM.getKey(item));
    }

    @Nullable
    @Override
    public TagKey<Item> getRelevantItemTag(Holder<Item> item) {
        return getRelevantItemTag(item.value());
    }

    @Nullable
    @Override
    public UnifyEntry<Item> getItemReplacement(ResourceLocation item) {
        var t = getRelevantItemTag(item);
        if (t == null) {
            return null;
        }

        if (stoneVariantLookup.isOreTag(t)) {
            String stone = stoneVariantLookup.getStoneVariant(item);
            return getTagTargetItem(t, i -> stone.equals(stoneVariantLookup.getStoneVariant(i)));
        }

        return getTagTargetItem(t);
    }

    @Nullable
    @Override
    public UnifyEntry<Item> getItemReplacement(Item item) {
        return getItemReplacement(BuiltInRegistries.ITEM.getKey(item));
    }

    @Nullable
    @Override
    public UnifyEntry<Item> getItemReplacement(Holder<Item> item) {
        return getItemReplacement(BuiltInRegistries.ITEM.getKey(item.value()));
    }

    @Nullable
    @Override
    public UnifyEntry<Item> getTagTargetItem(TagKey<Item> tag) {
        return getTagTargetItem(tag, i -> true);
    }

    @Nullable
    @Override
    public UnifyEntry<Item> getTagTargetItem(TagKey<Item> tag, Predicate<ResourceLocation> itemFilter) {
        var tagToLookup = tagSubstitutions.getSubstituteTag(tag);
        if (tagToLookup == null) tagToLookup = tag;

        var items = getEntries(tagToLookup)
                .stream()
                .filter(entry -> itemFilter.test(entry.id()))
                // clean stone variant first in case of a stone variant tag
                .sorted(Comparator.comparingInt(value -> value.id().toString().length()))
                .toList();

        if (items.isEmpty()) return null;

        return modPriorities.findTargetItem(tagToLookup, items);
    }

    @Override
    public boolean isItemInUnifiedIngredient(Ingredient ingredient, ItemStack item) {
        Set<TagKey<Item>> checkedTags = new HashSet<>();

        for (ItemStack ingredItem : ingredient.getItems()) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(ingredItem.getItem());

            var relevantTag = getRelevantItemTag(itemId);
            if (relevantTag == null || checkedTags.contains(relevantTag)) continue;
            checkedTags.add(relevantTag);

            if (item.is(relevantTag)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public TagSubstitutions getTagSubstitutions() {
        return tagSubstitutions;
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

        public UnifyLookup build(ModPriorities modPriorities, StoneVariantLookup stoneVariantLookup, TagSubstitutions tagSubstitutions) {
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
            return new UnifyLookupImpl(modPriorities,
                    map,
                    idEntriesToTag.build(),
                    stoneVariantLookup,
                    tagSubstitutions);
        }
    }
}

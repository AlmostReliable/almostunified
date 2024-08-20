package com.almostreliable.unified.impl;

import com.almostreliable.unified.api.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public final class UnificationLookupImpl implements UnificationLookup {

    private final ModPriorities modPriorities;
    private final StoneVariantLookup stoneVariantLookup;
    private final TagSubstitutions tagSubstitutions;
    private final Map<TagKey<Item>, Set<UnificationEntry<Item>>> tagsToEntries;
    private final Map<ResourceLocation, UnificationEntry<Item>> idsToEntries;

    private UnificationLookupImpl(ModPriorities modPriorities, StoneVariantLookup stoneVariantLookup, TagSubstitutions tagSubstitutions, Map<TagKey<Item>, Set<UnificationEntry<Item>>> tagsToEntries, Map<ResourceLocation, UnificationEntry<Item>> idsToEntries) {
        this.modPriorities = modPriorities;
        this.stoneVariantLookup = stoneVariantLookup;
        this.tagSubstitutions = tagSubstitutions;
        this.tagsToEntries = tagsToEntries;
        this.idsToEntries = idsToEntries;
    }

    @Override
    public Collection<TagKey<Item>> getTags() {
        return tagsToEntries.keySet();
    }

    @Override
    public Collection<UnificationEntry<Item>> getTagEntries(TagKey<Item> tag) {
        return tagsToEntries.getOrDefault(tag, Collections.emptySet());
    }

    @Nullable
    @Override
    public UnificationEntry<Item> getItemEntry(ResourceLocation item) {
        return idsToEntries.get(item);
    }

    @Nullable
    @Override
    public TagKey<Item> getRelevantItemTag(ResourceLocation item) {
        UnificationEntry<Item> entry = idsToEntries.get(item);
        return entry == null ? null : entry.tag();
    }

    @Nullable
    @Override
    public UnificationEntry<Item> getItemReplacement(ResourceLocation item) {
        var tag = getRelevantItemTag(item);
        if (tag == null) return null;

        if (stoneVariantLookup.isOreTag(tag)) {
            String stoneVariant = stoneVariantLookup.getStoneVariant(item);
            return getTagTargetItem(tag, itemId -> stoneVariant.equals(stoneVariantLookup.getStoneVariant(itemId)));
        }

        return getTagTargetItem(tag);
    }

    @Nullable
    @Override
    public UnificationEntry<Item> getTagTargetItem(TagKey<Item> tag, Predicate<ResourceLocation> itemFilter) {
        var substituteTag = tagSubstitutions.getSubstituteTag(tag);
        var tagToCheck = substituteTag != null ? substituteTag : tag;

        var items = getTagEntries(tagToCheck)
                .stream()
                .filter(entry -> itemFilter.test(entry.id()))
                // sort by length so clean stone variants come first
                .sorted(Comparator.comparingInt(value -> value.id().toString().length()))
                .toList();

        return items.isEmpty() ? null : modPriorities.findTargetItem(tagToCheck, items);
    }

    @Override
    public boolean isUnifiedIngredientItem(Ingredient ingredient, ItemStack item) {
        Set<TagKey<Item>> checkedTags = new HashSet<>();

        for (ItemStack stack : ingredient.getItems()) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());

            var relevantTag = getRelevantItemTag(itemId);
            if (relevantTag == null || checkedTags.contains(relevantTag)) continue;
            checkedTags.add(relevantTag);

            if (item.is(relevantTag)) {
                return true;
            }
        }

        return false;
    }

    public static class Builder {

        private final Set<UnificationEntry<Item>> createdEntries = new HashSet<>();
        private final Map<TagKey<Item>, Set<UnificationEntry<Item>>> tagsToEntries = new HashMap<>();

        private void put(TagKey<Item> tag, UnificationEntry<Item> entry) {
            if (createdEntries.contains(entry)) {
                throw new IllegalStateException("entry " + entry + " already created");
            }

            createdEntries.add(entry);
            tagsToEntries.computeIfAbsent(tag, $ -> new HashSet<>()).add(entry);
        }

        public Builder put(TagKey<Item> tag, ResourceLocation... ids) {
            for (ResourceLocation id : ids) {
                put(tag, new UnificationEntryImpl<>(BuiltInRegistries.ITEM, id));
            }

            return this;
        }

        public Builder put(TagKey<Item> tag, Item... items) {
            for (var item : items) {
                put(tag, new UnificationEntryImpl<>(BuiltInRegistries.ITEM, item));
            }

            return this;
        }

        public UnificationLookup build(ModPriorities modPriorities, StoneVariantLookup stoneVariantLookup, TagSubstitutions tagSubstitutions) {
            ImmutableMap.Builder<TagKey<Item>, Set<UnificationEntry<Item>>> tagsToEntriesBuilder = ImmutableMap.builder();
            ImmutableMap.Builder<ResourceLocation, UnificationEntry<Item>> idsToEntriesBuilder = ImmutableMap.builder();

            tagsToEntries.forEach((tag, entries) -> {
                ImmutableSet.Builder<UnificationEntry<Item>> entrySetBuilder = ImmutableSet.builder();
                for (var entry : entries) {
                    entrySetBuilder.add(entry);
                    ((UnificationEntryImpl<Item>) entry).bindTag(tag);
                    idsToEntriesBuilder.put(entry.id(), entry);
                }

                tagsToEntriesBuilder.put(tag, entrySetBuilder.build());
            });

            return new UnificationLookupImpl(
                    modPriorities,
                    stoneVariantLookup,
                    tagSubstitutions,
                    tagsToEntriesBuilder.build(),
                    idsToEntriesBuilder.build()
            );
        }
    }
}

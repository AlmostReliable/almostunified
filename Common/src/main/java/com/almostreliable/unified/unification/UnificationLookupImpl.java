package com.almostreliable.unified.unification;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import com.almostreliable.unified.api.unification.ModPriorities;
import com.almostreliable.unified.api.unification.StoneVariants;
import com.almostreliable.unified.api.unification.TagSubstitutions;
import com.almostreliable.unified.api.unification.UnificationEntry;
import com.almostreliable.unified.api.unification.UnificationLookup;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public final class UnificationLookupImpl implements UnificationLookup {

    private final ModPriorities modPriorities;
    private final StoneVariants stoneVariants;
    private final TagSubstitutions tagSubstitutions;
    private final Map<TagKey<Item>, Set<UnificationEntry<Item>>> tagsToEntries;
    private final Map<ResourceLocation, UnificationEntry<Item>> idsToEntries;

    private UnificationLookupImpl(ModPriorities modPriorities, StoneVariants stoneVariants, TagSubstitutions tagSubstitutions, Map<TagKey<Item>, Set<UnificationEntry<Item>>> tagsToEntries, Map<ResourceLocation, UnificationEntry<Item>> idsToEntries) {
        this.modPriorities = modPriorities;
        this.stoneVariants = stoneVariants;
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
    public UnificationEntry<Item> getVariantItemTarget(ResourceLocation item) {
        var tag = getRelevantItemTag(item);
        if (tag == null) return null;

        if (stoneVariants.isOreTag(tag)) {
            String stoneVariant = stoneVariants.getStoneVariant(item);
            return getTagTargetItem(tag, itemId -> stoneVariant.equals(stoneVariants.getStoneVariant(itemId)));
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

        public UnificationLookup build(ModPriorities modPriorities, StoneVariants stoneVariants, TagSubstitutions tagSubstitutions) {
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
                stoneVariants,
                tagSubstitutions,
                tagsToEntriesBuilder.build(),
                idsToEntriesBuilder.build()
            );
        }
    }
}

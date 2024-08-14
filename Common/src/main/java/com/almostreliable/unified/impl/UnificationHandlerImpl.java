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

public class UnificationHandlerImpl implements UnificationHandler {

    private final ModPriorities modPriorities;
    private final StoneVariantLookup stoneVariantLookup;
    private final TagSubstitutions tagSubstitutions;
    private final Map<TagKey<Item>, Set<UnificationEntry<Item>>> entries;
    private final Map<ResourceLocation, UnificationEntry<Item>> idEntriesToTag;

    private UnificationHandlerImpl(ModPriorities modPriorities, Map<TagKey<Item>, Set<UnificationEntry<Item>>> entries, Map<ResourceLocation, UnificationEntry<Item>> idEntriesToTag, StoneVariantLookup stoneVariantLookup, TagSubstitutions tagSubstitutions) {
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
    public Collection<UnificationEntry<Item>> getEntries(TagKey<Item> tag) {
        return entries.getOrDefault(tag, Collections.emptySet());
    }

    @Nullable
    @Override
    public UnificationEntry<Item> getEntry(ResourceLocation entry) {
        return idEntriesToTag.get(entry);
    }

    @Nullable
    @Override
    public UnificationEntry<Item> getEntry(Item item) {
        return getEntry(BuiltInRegistries.ITEM.getKey(item));
    }

    @Nullable
    @Override
    public TagKey<Item> getRelevantItemTag(ResourceLocation item) {
        UnificationEntry<Item> entry = idEntriesToTag.get(item);
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
    public UnificationEntry<Item> getItemReplacement(ResourceLocation item) {
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
    public UnificationEntry<Item> getItemReplacement(Item item) {
        return getItemReplacement(BuiltInRegistries.ITEM.getKey(item));
    }

    @Nullable
    @Override
    public UnificationEntry<Item> getItemReplacement(Holder<Item> item) {
        return getItemReplacement(BuiltInRegistries.ITEM.getKey(item.value()));
    }

    @Nullable
    @Override
    public UnificationEntry<Item> getTagTargetItem(TagKey<Item> tag) {
        return getTagTargetItem(tag, i -> true);
    }

    @Nullable
    @Override
    public UnificationEntry<Item> getTagTargetItem(TagKey<Item> tag, Predicate<ResourceLocation> itemFilter) {
        var relevantTag = tagSubstitutions.getSubstituteTag(tag);
        if (relevantTag == null) relevantTag = tag;

        var items = getEntries(relevantTag)
                .stream()
                .filter(entry -> itemFilter.test(entry.id()))
                // clean stone variant first in case of a stone variant tag
                .sorted(Comparator.comparingInt(value -> value.id().toString().length()))
                .toList();

        if (items.isEmpty()) return null;

        return modPriorities.findTargetItem(relevantTag, items);
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
        private final Map<TagKey<Item>, Set<UnificationEntry<Item>>> entries = new HashMap<>();
        private final Set<UnificationEntry<Item>> createdEntries = new HashSet<>();

        private void put(TagKey<Item> tag, UnificationEntry<Item> entry) {
            if (createdEntries.contains(entry)) {
                throw new IllegalStateException("Entry already created: " + entry);
            }

            createdEntries.add(entry);
            entries.computeIfAbsent(tag, k -> new HashSet<>()).add(entry);
        }

        public Builder put(TagKey<Item> tag, ResourceLocation... ids) {
            for (var id : ids) {
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

        public UnificationHandler build(ModPriorities modPriorities, StoneVariantLookup stoneVariantLookup, TagSubstitutions tagSubstitutions) {
            ImmutableMap.Builder<TagKey<Item>, Set<UnificationEntry<Item>>> entries = ImmutableMap.builder();
            ImmutableMap.Builder<ResourceLocation, UnificationEntry<Item>> idEntriesToTag = ImmutableMap.builder();
            this.entries.forEach((t, e) -> {
                ImmutableSet.Builder<UnificationEntry<Item>> set = ImmutableSet.builder();
                for (UnificationEntry<Item> entry : e) {
                    set.add(entry);
                    ((UnificationEntryImpl<Item>) entry).bindTag(t);
                    idEntriesToTag.put(entry.id(), entry);
                }

                entries.put(t, set.build());
            });

            var map = entries.build();
            return new UnificationHandlerImpl(modPriorities,
                    map,
                    idEntriesToTag.build(),
                    stoneVariantLookup,
                    tagSubstitutions);
        }
    }
}

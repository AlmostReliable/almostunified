package com.almostreliable.unified.utils;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class TagInheritance {

    private TagInheritance() {}

    // TODO: add blacklist
    // TODO: add global switch
    // TODO: add logging

    public static void applyInheritance(TagMap globalTagMap, TagMap filteredTagMap, ReplacementMap repMap, Map<ResourceLocation, Collection<Holder<Item>>> rawTags) {
        var relations = resolveRelations(filteredTagMap, repMap);

        for (RelationEntry relation : relations) {
            var dominantHolder = findDominantHolder(rawTags, relation);
            if (dominantHolder == null) continue;

            for (var item : relation.items) {
                var itemTags = globalTagMap.getTagsByItem(item);

                for (var itemTag : itemTags) {
                    var itemTagHolders = rawTags.get(itemTag.location());
                    if (itemTagHolders == null) continue;

                    ImmutableSet.Builder<Holder<Item>> newHolders = ImmutableSet.builder();
                    newHolders.addAll(itemTagHolders);
                    newHolders.add(dominantHolder);

                    rawTags.put(itemTag.location(), newHolders.build());
                }
            }
        }
    }

    private static Set<RelationEntry> resolveRelations(TagMap filteredTagMap, ReplacementMap repMap) {
        Set<RelationEntry> relations = new HashSet<>();

        for (var unifyTag : filteredTagMap.getTags()) {
            var itemsByTag = filteredTagMap.getItemsByTag(unifyTag);

            // avoid handling single entries and tags that only contain the same namespace for all items
            long namespaces = itemsByTag.stream().map(ResourceLocation::getNamespace).distinct().count();
            if (namespaces <= 1) continue;

            ResourceLocation dominant = repMap.getPreferredItemForTag(unifyTag, $ -> true);
            if (dominant == null || !BuiltInRegistries.ITEM.containsKey(dominant)) continue;

            Set<ResourceLocation> items = itemsByTag.stream()
                    .filter(item -> !item.equals(dominant))
                    .filter(BuiltInRegistries.ITEM::containsKey)
                    .collect(Collectors.toSet());

            if (items.isEmpty()) continue;
            relations.add(new RelationEntry(unifyTag.location(), dominant, items));
        }

        return relations;
    }

    @Nullable
    private static Holder<Item> findDominantHolder(Map<ResourceLocation, Collection<Holder<Item>>> rawTags, RelationEntry relation) {
        var tagHolders = rawTags.get(relation.tag);
        if (tagHolders == null) return null;

        for (var tagHolder : tagHolders) {
            var holderKey = tagHolder.unwrapKey();
            if (holderKey.isPresent() && holderKey.get().location().equals(relation.dominant)) {
                return tagHolder;
            }
        }

        return null;
    }

    private record RelationEntry(ResourceLocation tag, ResourceLocation dominant, Set<ResourceLocation> items) {}
}

package com.almostreliable.unified;

import com.almostreliable.unified.api.AlmostUnifiedLookup;
import com.google.auto.service.AutoService;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.stream.Collectors;

@AutoService(AlmostUnifiedLookup.class)
public class AlmostUnifiedLookupImpl implements AlmostUnifiedLookup {

    @Override
    public boolean isLoaded() {
        return AlmostUnified.isRuntimeLoaded();
    }

    @Nullable
    @Override
    public Item getReplacementForItem(ItemLike itemLike) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(itemLike.asItem());
        return AlmostUnified
                .getRuntime()
                .getReplacementMap()
                .map(rm -> rm.getReplacementForItem(id))
                .flatMap(BuiltInRegistries.ITEM::getOptional)
                .orElse(null);
    }

    @Nullable
    @Override
    public Item getPreferredItemForTag(TagKey<Item> tag) {
        return AlmostUnified
                .getRuntime()
                .getReplacementMap()
                .map(rm -> rm.getPreferredItemForTag(tag))
                .flatMap(BuiltInRegistries.ITEM::getOptional)
                .orElse(null);
    }

    @Nullable
    @Override
    public TagKey<Item> getPreferredTagForItem(ItemLike itemLike) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(itemLike.asItem());
        return AlmostUnified
                .getRuntime()
                .getReplacementMap()
                .map(rm -> rm.getPreferredTagForItem(id))
                .map(ut -> TagKey.create(Registries.ITEM, ut.location()))
                .orElse(null);
    }

    @Override
    public Set<Item> getPotentialItems(TagKey<Item> tag) {
        return AlmostUnified
                .getRuntime()
                .getFilteredTagMap()
                .map(tagMap -> tagMap
                        .getEntriesByTag(tag)
                        .stream()
                        .flatMap(rl -> BuiltInRegistries.ITEM.getOptional(rl).stream())
                        .collect(Collectors.toSet()))
                .orElseGet(Set::of);
    }

    @Override
    public Set<TagKey<Item>> getConfiguredTags() {
        return AlmostUnified
                .getRuntime()
                .getFilteredTagMap()
                .map(tagMap -> tagMap
                        .getTags()
                        .stream()
                        .map(ut -> TagKey.create(Registries.ITEM, ut.location()))
                        .collect(Collectors.toSet()))
                .orElseGet(Set::of);
    }
}

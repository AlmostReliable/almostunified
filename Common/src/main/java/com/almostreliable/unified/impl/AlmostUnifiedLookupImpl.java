package com.almostreliable.unified.impl;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.api.AlmostUnifiedLookup;
import com.google.auto.service.AutoService;
import net.minecraft.core.registries.BuiltInRegistries;
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
        ResourceLocation replacementId = AlmostUnified.getRuntime().getUnifyLookup().getReplacementForItem(id);
        if (replacementId == null) {
            return null;
        }

        return BuiltInRegistries.ITEM.getOptional(replacementId).orElse(null);
    }

    @Nullable
    @Override
    public Item getPreferredItemForTag(TagKey<Item> tag) {
        ResourceLocation itemId = AlmostUnified.getRuntime().getUnifyLookup().getPreferredItemForTag(tag);
        if (itemId == null) {
            return null;
        }

        return BuiltInRegistries.ITEM.getOptional(itemId).orElse(null);
    }

    @Nullable
    @Override
    public TagKey<Item> getPreferredTagForItem(ItemLike itemLike) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(itemLike.asItem());
        return AlmostUnified.getRuntime().getUnifyLookup().getPreferredTagForItem(id);
    }

    @Override
    public Set<Item> getPotentialItems(TagKey<Item> tag) {
        Set<ResourceLocation> entries = AlmostUnified.getRuntime().getTagMap().getEntriesByTag(tag);

        return entries
                .stream()
                .flatMap(rl -> BuiltInRegistries.ITEM.getOptional(rl).stream())
                .collect(Collectors.toSet());
    }

    @Override
    public Set<TagKey<Item>> getConfiguredTags() {
        return AlmostUnified.getRuntime().getTagMap().getTags();
    }
}

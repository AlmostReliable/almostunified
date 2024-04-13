package com.almostreliable.unified.impl;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.api.AlmostUnifiedLookup;
import com.almostreliable.unified.api.UnifyEntry;
import com.google.auto.service.AutoService;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import java.util.Collection;
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
        var replacement = AlmostUnified.getRuntime().getUnifyLookup().getReplacementForItem(id);
        if (replacement == null) {
            return null;
        }

        return replacement.value();
    }

    @Nullable
    @Override
    public Item getPreferredItemForTag(TagKey<Item> tag) {
        var replacement = AlmostUnified.getRuntime().getUnifyLookup().getPreferredItemForTag(tag);
        if (replacement == null) {
            return null;
        }

        return replacement.value();
    }

    @Nullable
    @Override
    public TagKey<Item> getPreferredTagForItem(ItemLike itemLike) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(itemLike.asItem());
        return AlmostUnified.getRuntime().getUnifyLookup().getPreferredTagForItem(id);
    }

    @Override
    public Set<Item> getPotentialItems(TagKey<Item> tag) {
        var entries = AlmostUnified.getRuntime().getUnifyLookup().getEntries(tag);

        return entries
                .stream()
                .map(UnifyEntry::value)
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<TagKey<Item>> getAllUnifiedTags() {
        return AlmostUnified.getRuntime().getUnifyLookup().getUnifiedTags();
    }
}

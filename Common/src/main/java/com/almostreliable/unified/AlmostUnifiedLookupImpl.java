package com.almostreliable.unified;

import com.almostreliable.unified.api.AlmostUnifiedLookup;
import com.almostreliable.unified.api.AlmostUnifiedRuntime;
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

    @Override
    public AlmostUnifiedRuntime getRuntimeOrThrow() {
        AlmostUnifiedRuntime runtime = AlmostUnified.getRuntime();
        if (runtime == null) {
            throw new IllegalStateException("The runtime is not loaded");
        }

        return runtime;
    }

    @Nullable
    @Override
    public AlmostUnifiedRuntime getRuntime() {
        return AlmostUnified.getRuntime();
    }

    @Nullable
    @Override
    public Item getReplacementForItem(ItemLike itemLike) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(itemLike.asItem());
        AlmostUnifiedRuntime runtime = getRuntime();
        if (runtime == null) {
            return null;
        }

        var replacement = runtime.getUnifyLookup().getReplacementForItem(id);
        if (replacement == null) {
            return null;
        }

        return replacement.value();
    }

    @Nullable
    @Override
    public Item getPreferredItemForTag(TagKey<Item> tag) {
        AlmostUnifiedRuntime runtime = getRuntime();
        if (runtime == null) {
            return null;
        }

        var replacement = runtime.getUnifyLookup().getPreferredItemForTag(tag);
        if (replacement == null) {
            return null;
        }

        return replacement.value();
    }

    @Nullable
    @Override
    public TagKey<Item> getPreferredTagForItem(ItemLike itemLike) {
        AlmostUnifiedRuntime runtime = getRuntime();
        if (runtime == null) {
            return null;
        }

        return runtime.getUnifyLookup().getPreferredTagForItem(itemLike.asItem());
    }

    @Override
    public Set<Item> getPotentialItems(TagKey<Item> tag) {
        AlmostUnifiedRuntime runtime = getRuntime();
        if (runtime == null) {
            return Set.of();
        }

        var entries = runtime.getUnifyLookup().getEntries(tag);

        return entries
                .stream()
                .map(UnifyEntry::value)
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<TagKey<Item>> getAllUnifiedTags() {
        AlmostUnifiedRuntime runtime = getRuntime();
        if (runtime == null) {
            return Set.of();
        }

        return runtime.getUnifyLookup().getUnifiedTags();
    }
}

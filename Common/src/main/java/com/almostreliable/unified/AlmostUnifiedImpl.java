package com.almostreliable.unified;

import com.almostreliable.unified.api.AlmostUnified;
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

@AutoService(AlmostUnified.class)
public class AlmostUnifiedImpl implements AlmostUnified {

    @Override
    public boolean isRuntimeLoaded() {
        return getRuntime() != null;
    }

    @Nullable
    @Override
    public AlmostUnifiedRuntime getRuntime() {
        return AlmostUnifiedCommon.getRuntime();
    }

    @Override
    public AlmostUnifiedRuntime getRuntimeOrThrow() {
        AlmostUnifiedRuntime runtime = AlmostUnifiedCommon.getRuntime();
        if (runtime == null) {
            throw new IllegalStateException("The runtime is not loaded");
        }

        return runtime;
    }

    @Nullable
    @Override
    public Item getReplacementForItem(ItemLike itemLike) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(itemLike.asItem());
        AlmostUnifiedRuntime runtime = getRuntime();
        if (runtime == null) {
            return null;
        }

        var replacement = runtime.getUnifyLookup().getItemReplacement(id);
        if (replacement == null) {
            return null;
        }

        return replacement.value();
    }

    @Nullable
    @Override
    public Item getTagTargetItem(TagKey<Item> tag) {
        AlmostUnifiedRuntime runtime = getRuntime();
        if (runtime == null) {
            return null;
        }

        var replacement = runtime.getUnifyLookup().getTagTargetItem(tag);
        if (replacement == null) {
            return null;
        }

        return replacement.value();
    }

    @Nullable
    @Override
    public TagKey<Item> getRelevantItemTag(ItemLike itemLike) {
        AlmostUnifiedRuntime runtime = getRuntime();
        if (runtime == null) {
            return null;
        }

        return runtime.getUnifyLookup().getRelevantItemTag(itemLike.asItem());
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
    public Collection<TagKey<Item>> getUnifiedTags() {
        AlmostUnifiedRuntime runtime = getRuntime();
        if (runtime == null) {
            return Set.of();
        }

        return runtime.getUnifyLookup().getUnifiedTags();
    }
}

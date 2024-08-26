package com.almostreliable.unified.core;

import com.almostreliable.unified.AlmostUnifiedCommon;
import com.almostreliable.unified.api.AlmostUnified;
import com.almostreliable.unified.api.AlmostUnifiedRuntime;
import com.almostreliable.unified.api.unification.UnificationEntry;
import com.google.auto.service.AutoService;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

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
            throw new IllegalStateException("runtime is not loaded");
        }

        return runtime;
    }

    @Override
    public Collection<TagKey<Item>> getTags() {
        if (!isRuntimeLoaded()) return Set.of();
        return getRuntimeOrThrow().getUnificationLookup().getTags();
    }

    @Override
    public Collection<Item> getTagEntries(TagKey<Item> tag) {
        if (!isRuntimeLoaded()) return Set.of();
        return getRuntimeOrThrow()
            .getUnificationLookup()
            .getTagEntries(tag)
            .stream()
            .map(UnificationEntry::value)
            .collect(Collectors.toSet());
    }

    @Nullable
    @Override
    public TagKey<Item> getRelevantItemTag(ItemLike itemLike) {
        if (!isRuntimeLoaded()) return null;
        return getRuntimeOrThrow().getUnificationLookup().getRelevantItemTag(itemLike.asItem());
    }

    @Nullable
    @Override
    public Item getVariantItemTarget(ItemLike itemLike) {
        if (!isRuntimeLoaded()) return null;

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(itemLike.asItem());
        var replacement = getRuntimeOrThrow().getUnificationLookup().getVariantItemTarget(id);
        return replacement == null ? null : replacement.value();
    }

    @Nullable
    @Override
    public Item getTagTargetItem(TagKey<Item> tag) {
        if (!isRuntimeLoaded()) return null;

        var replacement = getRuntimeOrThrow().getUnificationLookup().getTagTargetItem(tag);
        return replacement == null ? null : replacement.value();
    }
}

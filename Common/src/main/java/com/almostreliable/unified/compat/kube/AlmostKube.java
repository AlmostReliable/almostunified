package com.almostreliable.unified.compat.kube;

import com.almostreliable.unified.api.AlmostUnified;
import com.almostreliable.unified.api.AlmostUnifiedRuntime;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public final class AlmostKube {

    private AlmostKube() {}

    private static AlmostUnifiedRuntime getRuntime() {
        return AlmostUnified.INSTANCE.getRuntimeOrThrow();
    }

    public static Set<String> getTags() {
        return getRuntime()
                .getUnificationLookup()
                .getTags()
                .stream()
                .map(tag -> tag.location().toString())
                .collect(Collectors.toSet());
    }

    public static Set<String> getTagEntries(ResourceLocation tag) {
        var tagKey = TagKey.create(Registries.ITEM, tag);
        return getRuntime()
                .getUnificationLookup()
                .getTagEntries(tagKey)
                .stream()
                .map(holder -> holder.id().toString())
                .collect(Collectors.toSet());
    }

    @Nullable
    public static String getRelevantItemTag(ItemStack stack) {
        var tag = getRuntime().getUnificationLookup().getRelevantItemTag(getId(stack));
        return tag == null ? null : tag.location().toString();
    }

    public static ItemStack getVariantItemTarget(ItemStack stack) {
        var entry = getRuntime().getUnificationLookup().getVariantItemTarget(getId(stack));
        if (entry == null) return ItemStack.EMPTY;

        return entry.value().getDefaultInstance();
    }

    public static ItemStack getTagTargetItem(ResourceLocation tag) {
        var tagKey = TagKey.create(Registries.ITEM, tag);
        var entry = getRuntime().getUnificationLookup().getTagTargetItem(tagKey);
        if (entry == null) return ItemStack.EMPTY;

        return entry.value().getDefaultInstance();
    }

    private static ResourceLocation getId(ItemStack stack) {
        return BuiltInRegistries.ITEM
                .getResourceKey(stack.getItem())
                .map(ResourceKey::location)
                .orElseThrow(() -> new IllegalArgumentException("Item not found in registry"));
    }
}

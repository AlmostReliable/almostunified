package com.almostreliable.unified.compat;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.config.UnifyConfig;
import com.almostreliable.unified.utils.UnifyTag;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public final class AlmostKube {

    private AlmostKube() {}

    @Nullable
    public static String getPreferredTagForItem(ItemStack stack) {
        UnifyTag<Item> tag = AlmostUnified
                .getRuntime()
                .getReplacementMap()
                .orElseThrow(AlmostKube::notLoadedException)
                .getPreferredTagForItem(getId(stack));
        return tag == null ? null : tag.location().toString();
    }

    public static ItemStack getReplacementForItem(ItemStack stack) {
        ResourceLocation replacement = AlmostUnified
                .getRuntime()
                .getReplacementMap()
                .orElseThrow(AlmostKube::notLoadedException)
                .getReplacementForItem(getId(stack));
        return Registry.ITEM.get(replacement).getDefaultInstance();
    }

    public static ItemStack getPreferredItemForTag(ResourceLocation tag) {
        UnifyTag<Item> asUnifyTag = UnifyTag.item(tag);
        ResourceLocation item = AlmostUnified
                .getRuntime()
                .getReplacementMap()
                .orElseThrow(AlmostKube::notLoadedException)
                .getPreferredItemForTag(asUnifyTag, $ -> true);
        return Registry.ITEM.get(item).getDefaultInstance();
    }

    public static Set<String> getTags() {
        return AlmostUnified
                .getRuntime()
                .getFilteredTagMap()
                .orElseThrow(AlmostKube::notLoadedException)
                .getTags()
                .stream()
                .map(tag -> tag.location().toString())
                .collect(Collectors.toSet());
    }

    public static Set<String> getItemIds(ResourceLocation tag) {
        UnifyTag<Item> asUnifyTag = UnifyTag.item(tag);
        return AlmostUnified
                .getRuntime()
                .getFilteredTagMap()
                .orElseThrow(AlmostKube::notLoadedException)
                .getEntriesByTag(asUnifyTag)
                .stream()
                .map(ResourceLocation::toString)
                .collect(Collectors.toSet());
    }

    public static UnifyConfig getUnifyConfig() {
        return AlmostUnified.getRuntime().getUnifyConfig().orElseThrow(AlmostKube::notLoadedException);
    }

    private static ResourceLocation getId(ItemStack stack) {
        return Registry.ITEM
                .getResourceKey(stack.getItem())
                .map(ResourceKey::location)
                .orElseThrow(() -> new IllegalArgumentException("Item not found in registry"));
    }

    private static IllegalStateException notLoadedException() {
        return new IllegalStateException(
                "AlmostUnifiedRuntime is unavailable in KubeJS! Possible reasons: calling runtime too early, not in a server environment"
        );
    }
}

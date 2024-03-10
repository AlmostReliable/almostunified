package com.almostreliable.unified.compat;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.api.UnifyHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public final class AlmostKube {

    private AlmostKube() {}

    @Nullable
    public static String getPreferredTagForItem(ItemStack stack) {
        var tag = AlmostUnified
                .getRuntime()
                .getReplacementMap()
                .getPreferredTagForItem(getId(stack));
        return tag == null ? null : tag.location().toString();
    }

    public static ItemStack getReplacementForItem(ItemStack stack) {
        ResourceLocation replacement = AlmostUnified
                .getRuntime()
                .getReplacementMap()
                .getReplacementForItem(getId(stack));
        return BuiltInRegistries.ITEM.get(replacement).getDefaultInstance();
    }

    public static ItemStack getPreferredItemForTag(ResourceLocation tag) {
        var tagKey = TagKey.create(Registries.ITEM, tag);
        ResourceLocation item = AlmostUnified
                .getRuntime()
                .getReplacementMap()
                .getPreferredItemForTag(tagKey);
        return BuiltInRegistries.ITEM.get(item).getDefaultInstance();
    }

    public static Set<String> getTags() {
        return AlmostUnified
                .getRuntime()
                .getTagMap()
                .getTags()
                .stream()
                .map(tag -> tag.location().toString())
                .collect(Collectors.toSet());
    }

    public static Set<String> getItemIds(ResourceLocation tag) {
        var tagKey = TagKey.create(Registries.ITEM, tag);
        return AlmostUnified
                .getRuntime()
                .getTagMap()
                .getEntriesByTag(tagKey)
                .stream()
                .map(ResourceLocation::toString)
                .collect(Collectors.toSet());
    }

    public static Collection<? extends UnifyHandler> getUnifyHandlers() {
        return AlmostUnified
                .getRuntime()
                .getUnifyHandlers();
    }

    @Nullable
    public static UnifyHandler getUnifyHandler(String name) {
        return AlmostUnified
                .getRuntime()
                .getUnifyHandler(name);
    }

    private static ResourceLocation getId(ItemStack stack) {
        return BuiltInRegistries.ITEM
                .getResourceKey(stack.getItem())
                .map(ResourceKey::location)
                .orElseThrow(() -> new IllegalArgumentException("Item not found in registry"));
    }
}

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
import java.util.stream.StreamSupport;

@SuppressWarnings("unused")
public final class AlmostKube {

    private AlmostKube() {}

    @Nullable
    public static String getPreferredTagForItem(ItemStack stack) {
        var tag = AlmostUnified
                .getRuntime()
                .getUnifyLookup()
                .getPreferredTagForItem(getId(stack));
        return tag == null ? null : tag.location().toString();
    }

    public static ItemStack getReplacementForItem(ItemStack stack) {
        var entry = AlmostUnified
                .getRuntime()
                .getUnifyLookup()
                .getReplacementForItem(getId(stack));
        if (entry == null) {
            return ItemStack.EMPTY;
        }

        return entry.value().getDefaultInstance();
    }

    public static ItemStack getPreferredItemForTag(ResourceLocation tag) {
        var tagKey = TagKey.create(Registries.ITEM, tag);
        var entry = AlmostUnified
                .getRuntime()
                .getUnifyLookup()
                .getPreferredItemForTag(tagKey);
        if (entry == null) {
            return ItemStack.EMPTY;
        }

        return entry.value().getDefaultInstance();
    }

    public static Set<String> getTags() {
        return StreamSupport.stream(AlmostUnified
                        .getRuntime()
                        .getUnifyLookup()
                        .getUnifiedTags().spliterator(), false)
                .map(tag -> tag.location().toString())
                .collect(Collectors.toSet());
    }

    public static Set<String> getItemIds(ResourceLocation tag) {
        var tagKey = TagKey.create(Registries.ITEM, tag);
        return AlmostUnified
                .getRuntime()
                .getUnifyLookup()
                .getEntries(tagKey)
                .stream()
                .map(holder -> holder.id().toString())
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

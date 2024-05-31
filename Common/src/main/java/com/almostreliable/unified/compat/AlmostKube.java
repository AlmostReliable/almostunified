package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.AlmostUnifiedLookup;
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
        var tag = AlmostUnifiedLookup.INSTANCE
                .getRuntimeOrThrow()
                .getUnifyLookup()
                .getPreferredTagForItem(getId(stack));
        return tag == null ? null : tag.location().toString();
    }

    public static ItemStack getReplacementForItem(ItemStack stack) {
        var entry = AlmostUnifiedLookup.INSTANCE
                .getRuntimeOrThrow()
                .getUnifyLookup()
                .getReplacementForItem(getId(stack));
        if (entry == null) {
            return ItemStack.EMPTY;
        }

        return entry.value().getDefaultInstance();
    }

    public static ItemStack getPreferredItemForTag(ResourceLocation tag) {
        var tagKey = TagKey.create(Registries.ITEM, tag);
        var entry = AlmostUnifiedLookup.INSTANCE.getRuntimeOrThrow().getUnifyLookup().getPreferredItemForTag(tagKey);
        if (entry == null) {
            return ItemStack.EMPTY;
        }

        return entry.value().getDefaultInstance();
    }

    public static Set<String> getTags() {
        return AlmostUnifiedLookup.INSTANCE
                .getRuntimeOrThrow()
                .getUnifyLookup()
                .getUnifiedTags()
                .stream()
                .map(tag -> tag.location().toString())
                .collect(Collectors.toSet());
    }

    public static Set<String> getItemIds(ResourceLocation tag) {
        var tagKey = TagKey.create(Registries.ITEM, tag);
        return AlmostUnifiedLookup.INSTANCE.getRuntimeOrThrow()
                .getUnifyLookup()
                .getEntries(tagKey)
                .stream()
                .map(holder -> holder.id().toString())
                .collect(Collectors.toSet());
    }

    public static Collection<? extends UnifyHandler> getUnifyHandlers() {
        return AlmostUnifiedLookup.INSTANCE.getRuntimeOrThrow().getUnifyHandlers();
    }

    @Nullable
    public static UnifyHandler getUnifyHandler(String name) {
        return AlmostUnifiedLookup.INSTANCE.getRuntimeOrThrow().getUnifyHandler(name);
    }

    private static ResourceLocation getId(ItemStack stack) {
        return BuiltInRegistries.ITEM
                .getResourceKey(stack.getItem())
                .map(ResourceKey::location)
                .orElseThrow(() -> new IllegalArgumentException("Item not found in registry"));
    }
}

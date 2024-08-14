package com.almostreliable.unified.compat;

import com.almostreliable.unified.api.AlmostUnified;
import com.almostreliable.unified.api.AlmostUnifiedRuntime;
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
    public static String getRelevantItemTag(ItemStack stack) {
        var tag = getRuntime().getUnifyLookup().getRelevantItemTag(getId(stack));
        return tag == null ? null : tag.location().toString();
    }

    public static ItemStack getItemReplacement(ItemStack stack) {
        var entry = getRuntime().getUnifyLookup().getItemReplacement(getId(stack));
        if (entry == null) return ItemStack.EMPTY;

        return entry.value().getDefaultInstance();
    }

    public static ItemStack getTagTargetItem(ResourceLocation tag) {
        var tagKey = TagKey.create(Registries.ITEM, tag);
        var entry = getRuntime().getUnifyLookup().getTagTargetItem(tagKey);
        if (entry == null) return ItemStack.EMPTY;

        return entry.value().getDefaultInstance();
    }

    public static Set<String> getTags() {
        return getRuntime()
                .getUnifyLookup()
                .getUnifiedTags()
                .stream()
                .map(tag -> tag.location().toString())
                .collect(Collectors.toSet());
    }

    public static Set<String> getItemIds(ResourceLocation tag) {
        var tagKey = TagKey.create(Registries.ITEM, tag);
        return getRuntime()
                .getUnifyLookup()
                .getEntries(tagKey)
                .stream()
                .map(holder -> holder.id().toString())
                .collect(Collectors.toSet());
    }

    public static Collection<? extends UnifyHandler> getUnifyHandlers() {
        return getRuntime().getUnifyHandlers();
    }

    @Nullable
    public static UnifyHandler getUnifyHandler(String name) {
        return getRuntime().getUnifyHandler(name);
    }

    private static AlmostUnifiedRuntime getRuntime() {
        return AlmostUnified.INSTANCE.getRuntimeOrThrow();
    }

    private static ResourceLocation getId(ItemStack stack) {
        return BuiltInRegistries.ITEM
                .getResourceKey(stack.getItem())
                .map(ResourceKey::location)
                .orElseThrow(() -> new IllegalArgumentException("Item not found in registry"));
    }
}

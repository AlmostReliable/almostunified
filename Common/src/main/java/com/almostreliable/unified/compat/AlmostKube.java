package com.almostreliable.unified.compat;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.utils.UnifyTag;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.item.ItemStackJS;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.stream.Collectors;

public class AlmostKube extends KubeJSPlugin {

    @Override
    public void registerBindings(BindingsEvent event) {
        if (event.type == ScriptType.SERVER) {
            event.add("AlmostUnified", UnifyWrapper.class);
        }
    }

    public static class UnifyWrapper {
        @Nullable
        public static String getPreferredTagForItem(ItemStack stack) {
            UnifyTag<Item> tag = AlmostUnified
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
            return ItemStackJS.of(replacement);
        }

        public static ItemStack getPreferredItemForTag(ResourceLocation tag) {
            UnifyTag<Item> asUnifyTag = UnifyTag.item(tag);
            ResourceLocation item = AlmostUnified
                    .getRuntime()
                    .getReplacementMap()
                    .getPreferredItemForTag(asUnifyTag, $ -> true);
            return ItemStackJS.of(item);
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
            UnifyTag<Item> asUnifyTag = UnifyTag.item(tag);
            return AlmostUnified
                    .getRuntime()
                    .getTagMap()
                    .getItems(asUnifyTag)
                    .stream()
                    .map(ResourceLocation::toString)
                    .collect(Collectors.toSet());
        }

        private static ResourceLocation getId(ItemStack stack) {
            return Registry.ITEM
                    .getResourceKey(stack.getItem())
                    .map(ResourceKey::location)
                    .orElseThrow(() -> new IllegalArgumentException("Item not found in registry"));
        }
    }
}

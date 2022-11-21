package com.almostreliable.unified.compat;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.BuildConfig;
import com.almostreliable.unified.config.UnifyConfig;
import com.almostreliable.unified.utils.UnifyTag;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.item.ItemStackJS;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.Set;
import java.util.stream.Collectors;

public class AlmostKube extends KubeJSPlugin {

    @Override
    public void addBindings(BindingsEvent event) {
        if (event.type == ScriptType.SERVER) {
            event.add(BuildConfig.MOD_NAME, UnifyWrapper.class);
        }
    }

    public static class UnifyWrapper {
        public static String getPreferredTagForItem(ItemStackJS item) {
            UnifyTag<Item> tag = AlmostUnified
                    .getRuntime()
                    .getReplacementMap()
                    .getPreferredTagForItem(new ResourceLocation(item.getId()));
            return tag == null ? null : tag.location().toString();
        }

        public static ItemStackJS getReplacementForItem(ItemStackJS item) {
            ResourceLocation replacement = AlmostUnified
                    .getRuntime()
                    .getReplacementMap()
                    .getReplacementForItem(new ResourceLocation(item.getId()));
            return ItemStackJS.of(replacement);
        }

        public static ItemStackJS getPreferredItemForTag(ResourceLocation tag) {
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
                    .getFilteredTagMap()
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
                    .getItems(asUnifyTag)
                    .stream()
                    .map(ResourceLocation::toString)
                    .collect(Collectors.toSet());
        }

        public static UnifyConfig getUnifyConfig() {
            return AlmostUnified.getRuntime().getUnifyConfig();
        }
    }
}

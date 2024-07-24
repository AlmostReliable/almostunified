package com.almostreliable.unified.utils;

import com.almostreliable.unified.AlmostUnified;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public final class TagReloadHandler {

    private static final Object LOCK = new Object();
    @Nullable
    private static VanillaTagWrapper<Item> VANILLA_ITEM_TAGS;
    @Nullable
    private static VanillaTagWrapper<Block> VANILLA_BLOCK_TAGS;

    private TagReloadHandler() {}

    public static void initItemTags(Map<ResourceLocation, Collection<Holder<Item>>> rawItemTags) {
        synchronized (LOCK) {
            VANILLA_ITEM_TAGS = VanillaTagWrapper.of(BuiltInRegistries.ITEM, rawItemTags);
        }
    }

    public static void initBlockTags(Map<ResourceLocation, Collection<Holder<Block>>> rawBlockTags) {
        synchronized (LOCK) {
            VANILLA_BLOCK_TAGS = VanillaTagWrapper.of(BuiltInRegistries.BLOCK, rawBlockTags);
        }
    }

    public static void run() {
        if (VANILLA_ITEM_TAGS == null || VANILLA_BLOCK_TAGS == null) {
            return;
        }

        AlmostUnified.onTagLoaderReload(VANILLA_ITEM_TAGS, VANILLA_BLOCK_TAGS);

        VANILLA_ITEM_TAGS.seal();
        VANILLA_BLOCK_TAGS.seal();
        VANILLA_ITEM_TAGS = null;
        VANILLA_BLOCK_TAGS = null;
    }

    public static void applyCustomTags(Map<ResourceLocation, Set<ResourceLocation>> customTags, VanillaTagWrapper<Item> itemTags) {
        Multimap<ResourceLocation, ResourceLocation> changedItemTags = HashMultimap.create();

        for (var entry : customTags.entrySet()) {
            ResourceLocation tag = entry.getKey();
            Set<ResourceLocation> itemIds = entry.getValue();

            for (ResourceLocation itemId : itemIds) {
                if (!BuiltInRegistries.ITEM.containsKey(itemId)) {
                    AlmostUnified.LOGGER.warn("[CustomTags] Custom tag '{}' contains invalid item '{}'", tag, itemId);
                    continue;
                }

                ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, itemId);
                Holder<Item> itemHolder = BuiltInRegistries.ITEM.getHolder(itemKey).orElse(null);
                if (itemHolder == null) continue;

                var currentHolders = itemTags.get(tag);

                if (!currentHolders.isEmpty()) {
                    if (currentHolders.contains(itemHolder)) {
                        AlmostUnified.LOGGER.warn("[CustomTags] Custom tag '{}' already contains item '{}'",
                                tag,
                                itemId);
                        continue;
                    }
                }

                itemTags.add(tag, itemHolder);
                changedItemTags.put(tag, itemId);
            }
        }

        if (!changedItemTags.isEmpty()) {
            changedItemTags.asMap().forEach((tag, items) -> {
                AlmostUnified.LOGGER.info("[CustomTags] Modified tag '#{}', added {}", tag, items);
            });
        }
    }
}

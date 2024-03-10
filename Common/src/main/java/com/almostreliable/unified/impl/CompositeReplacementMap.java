package com.almostreliable.unified.impl;

import com.almostreliable.unified.api.ReplacementMap;
import com.almostreliable.unified.api.TagOwnerships;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public record CompositeReplacementMap(Iterable<? extends ReplacementMap> replacementMaps,
                                      TagOwnerships getTagOwnerships)
        implements ReplacementMap {

    @Nullable
    @Override
    public TagKey<Item> getPreferredTagForItem(ResourceLocation item) {
        for (var replacementMap : replacementMaps) {
            TagKey<Item> tag = replacementMap.getPreferredTagForItem(item);
            if (tag != null) {
                return tag;
            }
        }

        return null;
    }

    @Nullable
    @Override
    public ResourceLocation getReplacementForItem(ResourceLocation item) {
        for (var replacementMap : replacementMaps) {
            ResourceLocation replacement = replacementMap.getReplacementForItem(item);
            if (replacement != null) {
                return replacement;
            }
        }

        return null;
    }

    @Nullable
    @Override
    public ResourceLocation getPreferredItemForTag(TagKey<Item> tag) {
        for (var replacementMap : replacementMaps) {
            ResourceLocation replacement = replacementMap.getPreferredItemForTag(tag);
            if (replacement != null) {
                return replacement;
            }
        }

        return null;
    }

    @Nullable
    @Override
    public ResourceLocation getPreferredItemForTag(TagKey<Item> tag, Predicate<ResourceLocation> itemFilter) {
        for (var replacementMap : replacementMaps) {
            ResourceLocation replacement = replacementMap.getPreferredItemForTag(tag, itemFilter);
            if (replacement != null) {
                return replacement;
            }
        }

        return null;
    }

    @Override
    public boolean isItemInUnifiedIngredient(Ingredient ingred, ItemStack item) {
        for (var replacementMap : replacementMaps) {
            if (replacementMap.isItemInUnifiedIngredient(ingred, item)) {
                return true;
            }
        }

        return false;
    }
}

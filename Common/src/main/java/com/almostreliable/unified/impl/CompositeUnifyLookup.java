package com.almostreliable.unified.impl;

import com.almostreliable.unified.api.TagOwnerships;
import com.almostreliable.unified.api.UnifyEntry;
import com.almostreliable.unified.api.UnifyLookup;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public record CompositeUnifyLookup(Iterable<? extends UnifyLookup> unifyLookups,
                                   TagOwnerships getTagOwnerships)
        implements UnifyLookup {

    @Nullable
    @Override
    public TagKey<Item> getPreferredTagForItem(ResourceLocation item) {
        for (var unifyLookup : unifyLookups) {
            TagKey<Item> tag = unifyLookup.getPreferredTagForItem(item);
            if (tag != null) {
                return tag;
            }
        }

        return null;
    }

    @Nullable
    @Override
    public TagKey<Item> getPreferredTagForItem(Item item) {
        for (var unifyLookup : unifyLookups) {
            TagKey<Item> tag = unifyLookup.getPreferredTagForItem(item);
            if (tag != null) {
                return tag;
            }
        }

        return null;
    }

    @Nullable
    @Override
    public TagKey<Item> getPreferredTagForItem(Holder<Item> item) {
        for (var unifyLookup : unifyLookups) {
            TagKey<Item> tag = unifyLookup.getPreferredTagForItem(item);
            if (tag != null) {
                return tag;
            }
        }

        return null;
    }

    @Override
    public UnifyEntry<Item> getReplacementForItem(ResourceLocation item) {
        for (var unifyLookup : unifyLookups) {
            var resultItem = unifyLookup.getReplacementForItem(item);
            if (resultItem != null) {
                return resultItem;
            }
        }

        return null;
    }

    @Override
    public UnifyEntry<Item> getReplacementForItem(Item item) {
        for (var unifyLookup : unifyLookups) {
            var resultItem = unifyLookup.getReplacementForItem(item);
            if (resultItem != null) {
                return resultItem;
            }
        }

        return null;
    }

    @Override
    public UnifyEntry<Item> getReplacementForItem(Holder<Item> item) {
        for (var unifyLookup : unifyLookups) {
            var resultItem = unifyLookup.getReplacementForItem(item);
            if (resultItem != null) {
                return resultItem;
            }
        }

        return null;
    }

    @Override
    public UnifyEntry<Item> getPreferredItemForTag(TagKey<Item> tag) {
        for (var unifyLookup : unifyLookups) {
            var result = unifyLookup.getPreferredItemForTag(tag);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    @Override
    public UnifyEntry<Item> getPreferredItemForTag(TagKey<Item> tag, Predicate<ResourceLocation> itemFilter) {
        for (var unifyLookup : unifyLookups) {
            var result = unifyLookup.getPreferredItemForTag(tag, itemFilter);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    @Override
    public boolean isItemInUnifiedIngredient(Ingredient ingred, ItemStack item) {
        for (var unifyLookup : unifyLookups) {
            if (unifyLookup.isItemInUnifiedIngredient(ingred, item)) {
                return true;
            }
        }

        return false;
    }
}

package com.almostreliable.unified.api;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public interface UnifyLookup {
    @Nullable
    TagKey<Item> getPreferredTagForItem(ResourceLocation item);

    @Nullable
    TagKey<Item> getPreferredTagForItem(Item item);

    @Nullable
    TagKey<Item> getPreferredTagForItem(Holder<Item> item);

    @Nullable
    UnifyEntry<Item> getReplacementForItem(ResourceLocation item);

    @Nullable
    UnifyEntry<Item> getReplacementForItem(Item item);

    @Nullable
    UnifyEntry<Item> getReplacementForItem(Holder<Item> item);

    @Nullable
    default UnifyEntry<Item> getReplacementForItem(UnifyEntry<Item> item) {
        return getReplacementForItem(item.asHolder());
    }

    @Nullable
    UnifyEntry<Item> getPreferredItemForTag(TagKey<Item> tag);

    @Nullable
    UnifyEntry<Item> getPreferredItemForTag(TagKey<Item> tag, Predicate<ResourceLocation> itemFilter);

    /**
     * Gets all unify tags of the items within the given ingredient and checks
     * whether the given item is in one of those tags.
     *
     * @param ingred The ingredient to get the unify tags from.
     * @param item   The item to check.
     * @return Whether the item is in one of the unify tags of the ingredient.
     */
    boolean isItemInUnifiedIngredient(Ingredient ingred, ItemStack item);

    TagOwnerships getTagOwnerships();
}

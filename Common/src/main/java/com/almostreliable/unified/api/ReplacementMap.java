package com.almostreliable.unified.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public interface ReplacementMap {
    @Nullable
    TagKey<Item> getPreferredTagForItem(ResourceLocation item);

    @Nullable
    ResourceLocation getReplacementForItem(ResourceLocation item);

    @Nullable
    ResourceLocation getPreferredItemForTag(TagKey<Item> tag);

    @Nullable
    ResourceLocation getPreferredItemForTag(TagKey<Item> tag, Predicate<ResourceLocation> itemFilter);

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

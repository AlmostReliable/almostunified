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

    boolean isItemInUnifiedIngredient(Ingredient ingred, ItemStack item);

    TagOwnerships getTagOwnerships();
}

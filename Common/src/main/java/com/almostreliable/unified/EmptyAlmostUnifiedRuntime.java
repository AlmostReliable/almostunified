package com.almostreliable.unified;

import com.almostreliable.unified.api.*;
import com.almostreliable.unified.impl.TagMapImpl;
import com.almostreliable.unified.impl.TagOwnershipsImpl;
import com.almostreliable.unified.recipe.ModPrioritiesImpl;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class EmptyAlmostUnifiedRuntime implements AlmostUnifiedRuntime {

    @Override
    public void run(Map<ResourceLocation, JsonElement> recipes, boolean skipClientTracking) {
        // no-op
    }

    @Override
    public TagMap<Item> getFilteredTagMap() {
        return new TagMapImpl<>();
    }

    @Override
    public ReplacementMap getReplacementMap() {
        return new EmptyReplacementMap();
    }

    @Override
    public UnifySettings getUnifyConfig() {
        return new EmptyUnifySettings();
    }

    private static class EmptyUnifySettings implements UnifySettings {

        @Override
        public ModPriorities getModPriorities() {
            return new ModPrioritiesImpl(List.of(), new HashMap<>());
        }

        @Override
        public boolean shouldIncludeRecipeId(ResourceLocation id) {
            return false;
        }

        @Override
        public boolean shouldIncludeRecipeType(ResourceLocation type) {
            return false;
        }


        @Override
        public void clearCache() {

        }

        @Override
        public boolean hideNonPreferredItemsInRecipeViewers() {
            return false;
        }
    }

    private static class EmptyReplacementMap implements ReplacementMap {

        @Nullable
        @Override
        public TagKey<Item> getPreferredTagForItem(ResourceLocation item) {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getReplacementForItem(ResourceLocation item) {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getPreferredItemForTag(TagKey<Item> tag) {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getPreferredItemForTag(TagKey<Item> tag, Predicate<ResourceLocation> itemFilter) {
            return null;
        }

        @Override
        public boolean isItemInUnifiedIngredient(Ingredient ingred, ItemStack item) {
            return false;
        }

        @Override
        public TagOwnerships getTagOwnerships() {
            return new TagOwnershipsImpl();
        }
    }
}

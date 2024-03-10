package com.almostreliable.unified.impl;

import com.almostreliable.unified.api.*;
import com.almostreliable.unified.config.UnifyConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class UnifyHandlerImpl implements UnifyHandler {
    private final ModPriorities modPriorities;
    private final UnifyLookup unifyLookup;
    private final boolean recipeViewerHiding;
    private final TagMap<Item> tagMap;
    private final Set<Pattern> ignoredRecipes;
    private final Set<Pattern> ignoredRecipeTypes;
    private final Map<ResourceLocation, Boolean> ignoredRecipeTypesCache = new HashMap<>();
    private final String name;

    public static List<UnifyHandler> create(Collection<UnifyConfig> configs, TagMap<Item> tags, TagOwnershipsImpl tagOwnerships) {
        return configs
                .stream()
                .map(uc -> UnifyHandlerImpl.create(tags, uc, tagOwnerships))
                .toList();
    }

    public static UnifyHandler create(TagMap<Item> globalTagMap, UnifyConfig config, TagOwnerships tagOwnerships) {
        var unifyTags = config.getBakedTags();
        var filteredTagMap = globalTagMap.filtered(unifyTags::contains, config::includeItem);
        var stoneStrata = StoneStrataLookupImpl.create(config.getStoneStrata(), globalTagMap);

        return new UnifyHandlerImpl(
                config.getName(),
                config.getModPriorities(),
                stoneStrata,
                tagOwnerships,
                filteredTagMap,
                config.getIgnoredRecipes(),
                config.getIgnoredRecipeTypes(),
                config.hideNonPreferredItemsInRecipeViewers()
        );
    }

    public UnifyHandlerImpl(String name, ModPriorities modPriorities, StoneStrataLookup stoneStrata, TagOwnerships tagOwnerships, TagMap<Item> tagMap, Set<Pattern> ignoredRecipes, Set<Pattern> ignoredRecipeTypes, boolean recipeViewerHiding) {
        this.name = name;
        this.modPriorities = modPriorities;
        this.tagMap = tagMap;
        this.ignoredRecipes = ignoredRecipes;
        this.ignoredRecipeTypes = ignoredRecipeTypes;
        this.unifyLookup = new UnifyLookupImpl(modPriorities, tagMap, stoneStrata, tagOwnerships);
        this.recipeViewerHiding = recipeViewerHiding;
    }

    @Override
    public ModPriorities getModPriorities() {
        return modPriorities;
    }

    @Override
    public boolean shouldIncludeRecipeId(ResourceLocation id) {
        for (Pattern pattern : ignoredRecipes) {
            if (pattern.matcher(id.toString()).matches()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean shouldIncludeRecipeType(ResourceLocation type) {
        return ignoredRecipeTypesCache.computeIfAbsent(type, t -> {
            for (Pattern pattern : ignoredRecipeTypes) {
                if (pattern.matcher(t.toString()).matches()) {
                    return false;
                }
            }
            return true;
        });
    }

    @Override
    public void clearCache() {
        ignoredRecipeTypesCache.clear();
    }

    @Override
    public TagMap<Item> getTagMap() {
        return tagMap;
    }

    @Override
    public boolean hideNonPreferredItemsInRecipeViewers() {
        return recipeViewerHiding;
    }

    @Override
    public String getName() {
        return name;
    }

    @Nullable
    @Override
    public TagKey<Item> getPreferredTagForItem(ResourceLocation item) {
        return unifyLookup.getPreferredTagForItem(item);
    }

    @Nullable
    @Override
    public ResourceLocation getReplacementForItem(ResourceLocation item) {
        return unifyLookup.getReplacementForItem(item);
    }

    @Nullable
    @Override
    public ResourceLocation getPreferredItemForTag(TagKey<Item> tag) {
        return unifyLookup.getPreferredItemForTag(tag);
    }

    @Nullable
    @Override
    public ResourceLocation getPreferredItemForTag(TagKey<Item> tag, Predicate<ResourceLocation> itemFilter) {
        return unifyLookup.getPreferredItemForTag(tag, itemFilter);
    }

    @Override
    public boolean isItemInUnifiedIngredient(Ingredient ingred, ItemStack item) {
        return unifyLookup.isItemInUnifiedIngredient(ingred, item);
    }

    @Override
    public TagOwnerships getTagOwnerships() {
        return unifyLookup.getTagOwnerships();
    }
}

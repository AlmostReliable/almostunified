package com.almostreliable.unified.impl;

import com.almostreliable.unified.api.ModPriorities;
import com.almostreliable.unified.api.UnifySettings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.*;
import java.util.regex.Pattern;

public final class UnifySettingsImpl implements UnifySettings {
    private final ModPriorities modPriorities;
    private final Collection<String> stoneStrata;
    private final Set<TagKey<Item>> tags;
    private final Set<Pattern> ignoredItems;
    private final Set<Pattern> ignoredRecipes;
    private final Set<Pattern> ignoredRecipeTypes;
    private final Map<ResourceLocation, Boolean> ignoredRecipeTypesCache = new HashMap<>();

    public UnifySettingsImpl(ModPriorities modPriorities, Collection<String> stoneStrata, Set<TagKey<Item>> tags, Set<Pattern> ignoredItems, Set<Pattern> ignoredRecipes, Set<Pattern> ignoredRecipeTypes) {
        this.modPriorities = modPriorities;
        this.stoneStrata = Collections.unmodifiableCollection(stoneStrata);
        this.tags = Collections.unmodifiableSet(tags);
        this.ignoredItems = ignoredItems;
        this.ignoredRecipes = ignoredRecipes;
        this.ignoredRecipeTypes = ignoredRecipeTypes;
    }

    @Override
    public ModPriorities getModPriorities() {
        return modPriorities;
    }

    @Override
    public Collection<String> getStoneStrata() {
        return stoneStrata;
    }

    @Override
    public boolean shouldIncludeItem(ResourceLocation item) {
        for (Pattern pattern : ignoredItems) {
            if (pattern.matcher(item.toString()).matches()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean shouldIncludeRecipe(ResourceLocation recipe) {
        for (Pattern pattern : ignoredRecipes) {
            if (pattern.matcher(recipe.toString()).matches()) {
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
    public Set<TagKey<Item>> getTags() {
        return tags;
    }

    @Override
    public void clearCache() {
        ignoredRecipeTypesCache.clear();
    }
}

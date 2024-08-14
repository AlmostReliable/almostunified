package com.almostreliable.unified.impl;

import com.almostreliable.unified.api.*;
import com.almostreliable.unified.config.UnifyConfig;
import com.almostreliable.unified.utils.VanillaTagWrapper;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class ConfiguredUnificationHandlerImpl implements ConfiguredUnificationHandler {

    private final String name;
    private final ModPriorities modPriorities;
    private final UnificationHandler unificationHandler;
    private final Set<Pattern> ignoredRecipes;
    private final Set<Pattern> ignoredRecipeTypes;
    private final Set<Pattern> ignoredLootTables;
    private final boolean enableLootUnification;
    private final boolean recipeViewerHiding;

    private final Map<ResourceLocation, Boolean> ignoredRecipeTypesCache = new HashMap<>();

    private ConfiguredUnificationHandlerImpl(String name, ModPriorities modPriorities, UnificationHandler unificationHandler, Set<Pattern> ignoredRecipes, Set<Pattern> ignoredRecipeTypes, Set<Pattern> ignoredLootTables, boolean enableLootUnification, boolean recipeViewerHiding) {
        this.name = name;
        this.modPriorities = modPriorities;
        this.ignoredRecipes = ignoredRecipes;
        this.ignoredRecipeTypes = ignoredRecipeTypes;
        this.ignoredLootTables = ignoredLootTables;
        this.enableLootUnification = enableLootUnification;
        this.unificationHandler = unificationHandler;
        this.recipeViewerHiding = recipeViewerHiding;
    }

    public static List<ConfiguredUnificationHandler> create(Collection<UnifyConfig> configs, VanillaTagWrapper<Item> itemTags, VanillaTagWrapper<Block> blockTags, TagSubstitutionsImpl tagSubstitutions) {
        return configs
                .stream()
                .map(config -> create(config, itemTags, blockTags, tagSubstitutions))
                .toList();
    }

    public static ConfiguredUnificationHandler create(UnifyConfig config, VanillaTagWrapper<Item> itemTags, VanillaTagWrapper<Block> blockTags, TagSubstitutions tagSubstitutions) {
        var modPriorities = config.getModPriorities();
        var unifyTags = config.getBakedTags();
        var stoneVariantLookup = StoneVariantLookupImpl.create(config.getStoneVariants(), itemTags, blockTags);

        var builder = new UnificationHandlerImpl.Builder();
        itemTags.forEach((tag, holders) -> {
            if (!unifyTags.contains(tag)) {
                return;
            }

            for (Holder<Item> holder : holders) {
                holder.unwrapKey().ifPresent(key -> {
                    var id = key.location();
                    if (config.includeItem(id)) {
                        builder.put(tag, id);
                    }
                });
            }
        });

        return new ConfiguredUnificationHandlerImpl(
                config.getName(),
                modPriorities,
                builder.build(modPriorities, stoneVariantLookup, tagSubstitutions),
                config.getIgnoredRecipes(),
                config.getIgnoredRecipeTypes(),
                config.getIgnoredLootTables(),
                config.enableLootUnification(),
                config.shouldHideVariantItems()
        );
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
    public boolean shouldUnifyLootTable(ResourceLocation table) {
        for (Pattern pattern : ignoredLootTables) {
            if (pattern.matcher(table.toString()).matches()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean enableLootUnification() {
        return enableLootUnification;
    }

    @Override
    public void clearCache() {
        ignoredRecipeTypesCache.clear();
    }

    @Override
    public boolean shouldHideVariantItems() {
        return recipeViewerHiding;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Collection<TagKey<Item>> getUnifiedTags() {
        return unificationHandler.getUnifiedTags();
    }

    @Override
    public Collection<UnificationEntry<Item>> getEntries(TagKey<Item> tag) {
        return unificationHandler.getEntries(tag);
    }

    @Nullable
    @Override
    public UnificationEntry<Item> getEntry(ResourceLocation entry) {
        return unificationHandler.getEntry(entry);
    }

    @Nullable
    @Override
    public UnificationEntry<Item> getEntry(Item item) {
        return unificationHandler.getEntry(item);
    }

    @Nullable
    @Override
    public TagKey<Item> getRelevantItemTag(ResourceLocation item) {
        return unificationHandler.getRelevantItemTag(item);
    }

    @Nullable
    @Override
    public TagKey<Item> getRelevantItemTag(Item item) {
        return unificationHandler.getRelevantItemTag(item);
    }

    @Nullable
    @Override
    public TagKey<Item> getRelevantItemTag(Holder<Item> item) {
        return unificationHandler.getRelevantItemTag(item);
    }

    @Nullable
    @Override
    public UnificationEntry<Item> getItemReplacement(ResourceLocation item) {
        return unificationHandler.getItemReplacement(item);
    }

    @Nullable
    @Override
    public UnificationEntry<Item> getItemReplacement(Item item) {
        return unificationHandler.getItemReplacement(item);
    }

    @Nullable
    @Override
    public UnificationEntry<Item> getItemReplacement(Holder<Item> item) {
        return unificationHandler.getItemReplacement(item);
    }

    @Nullable
    @Override
    public UnificationEntry<Item> getTagTargetItem(TagKey<Item> tag) {
        return unificationHandler.getTagTargetItem(tag);
    }

    @Nullable
    @Override
    public UnificationEntry<Item> getTagTargetItem(TagKey<Item> tag, Predicate<ResourceLocation> itemFilter) {
        return unificationHandler.getTagTargetItem(tag, itemFilter);
    }

    @Override
    public boolean isItemInUnifiedIngredient(Ingredient ingredient, ItemStack item) {
        return unificationHandler.isItemInUnifiedIngredient(ingredient, item);
    }

    @Override
    public TagSubstitutions getTagSubstitutions() {
        return unificationHandler.getTagSubstitutions();
    }

}

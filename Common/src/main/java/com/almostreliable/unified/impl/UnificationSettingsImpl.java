package com.almostreliable.unified.impl;

import com.almostreliable.unified.api.*;
import com.almostreliable.unified.config.UnifyConfig;
import com.almostreliable.unified.utils.VanillaTagWrapper;
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

public final class UnificationSettingsImpl implements UnificationSettings {

    private final String name;
    private final ModPriorities modPriorities;
    private final Set<Pattern> ignoredRecipes;
    private final Set<Pattern> ignoredRecipeTypes;
    private final boolean recipeViewerHiding;
    private final boolean enableLootUnification;
    private final Set<Pattern> ignoredLootTables;
    private final UnificationLookup unificationLookup;

    private final Map<ResourceLocation, Boolean> ignoredRecipeTypesCache = new HashMap<>();

    private UnificationSettingsImpl(String name, ModPriorities modPriorities, Set<Pattern> ignoredRecipes, Set<Pattern> ignoredRecipeTypes, boolean recipeViewerHiding, boolean enableLootUnification, Set<Pattern> ignoredLootTables, UnificationLookup unificationLookup) {
        this.name = name;
        this.modPriorities = modPriorities;
        this.ignoredRecipes = ignoredRecipes;
        this.ignoredRecipeTypes = ignoredRecipeTypes;
        this.recipeViewerHiding = recipeViewerHiding;
        this.enableLootUnification = enableLootUnification;
        this.ignoredLootTables = ignoredLootTables;
        this.unificationLookup = unificationLookup;
    }

    public static List<UnificationSettings> create(Collection<UnifyConfig> configs, VanillaTagWrapper<Item> itemTags, VanillaTagWrapper<Block> blockTags, TagSubstitutionsImpl tagSubstitutions) {
        return configs
                .stream()
                .map(config -> create(config, itemTags, blockTags, tagSubstitutions))
                .toList();
    }

    public static UnificationSettings create(UnifyConfig config, VanillaTagWrapper<Item> itemTags, VanillaTagWrapper<Block> blockTags, TagSubstitutions tagSubstitutions) {
        var lookupBuilder = new UnificationLookupImpl.Builder();
        for (var tag : config.getBakedTags()) {
            var itemHolders = itemTags.get(tag);
            if (itemHolders.isEmpty()) continue;

            for (var itemHolder : itemHolders) {
                itemHolder.unwrapKey().ifPresent(itemKey -> {
                    var itemId = itemKey.location();
                    if (config.includeItem(itemId)) {
                        lookupBuilder.put(tag, itemId);
                    }
                });
            }
        }

        ModPriorities modPriorities = config.getModPriorities();
        StoneVariantLookup stoneVariantLookup = StoneVariantLookupImpl.create(
                config.getStoneVariants(),
                itemTags,
                blockTags
        );

        return new UnificationSettingsImpl(
                config.getName(),
                modPriorities,
                config.getIgnoredRecipes(),
                config.getIgnoredRecipeTypes(),
                config.shouldHideVariantItems(),
                config.enableLootUnification(),
                config.getIgnoredLootTables(),
                lookupBuilder.build(modPriorities, stoneVariantLookup, tagSubstitutions)
        );
    }

    @Override
    public String getName() {
        return name;
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
    public boolean shouldHideVariantItems() {
        return recipeViewerHiding;
    }

    @Override
    public boolean enableLootUnification() {
        return enableLootUnification;
    }

    @Override
    public boolean shouldIncludeLootTable(ResourceLocation table) {
        for (Pattern pattern : ignoredLootTables) {
            if (pattern.matcher(table.toString()).matches()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Collection<TagKey<Item>> getTags() {
        return unificationLookup.getTags();
    }

    @Override
    public Collection<UnificationEntry<Item>> getTagEntries(TagKey<Item> tag) {
        return unificationLookup.getTagEntries(tag);
    }

    @Nullable
    @Override
    public UnificationEntry<Item> getItemEntry(ResourceLocation item) {
        return unificationLookup.getItemEntry(item);
    }

    @Nullable
    @Override
    public TagKey<Item> getRelevantItemTag(ResourceLocation item) {
        return unificationLookup.getRelevantItemTag(item);
    }

    @Nullable
    @Override
    public UnificationEntry<Item> getItemReplacement(ResourceLocation item) {
        return unificationLookup.getItemReplacement(item);
    }

    @Nullable
    @Override
    public UnificationEntry<Item> getTagTargetItem(TagKey<Item> tag, Predicate<ResourceLocation> itemFilter) {
        return unificationLookup.getTagTargetItem(tag, itemFilter);
    }

    @Override
    public boolean isUnifiedIngredientItem(Ingredient ingredient, ItemStack item) {
        return unificationLookup.isUnifiedIngredientItem(ingredient, item);
    }

    public void clearCache() {
        ignoredRecipeTypesCache.clear();
    }
}

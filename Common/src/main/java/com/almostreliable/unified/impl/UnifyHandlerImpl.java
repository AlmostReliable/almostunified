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

public final class UnifyHandlerImpl implements UnifyHandler {
    private final ModPriorities modPriorities;
    private final UnifyLookup unifyLookup;
    private final boolean recipeViewerHiding;
    private final Set<Pattern> ignoredRecipes;
    private final Set<Pattern> ignoredRecipeTypes;
    private final Map<ResourceLocation, Boolean> ignoredRecipeTypesCache = new HashMap<>();
    private final Set<Pattern> ignoredLootTables;
    private final boolean enableLootUnification;
    private final String name;

    public static List<UnifyHandler> create(Collection<UnifyConfig> configs, VanillaTagWrapper<Item> itemTags, VanillaTagWrapper<Block> blockTags, TagSubstitutionsImpl tagSubstitutions) {
        return configs
                .stream()
                .map(config -> create(config, itemTags, blockTags, tagSubstitutions))
                .toList();
    }

    public static UnifyHandler create(UnifyConfig config, VanillaTagWrapper<Item> itemTags, VanillaTagWrapper<Block> blockTags, TagSubstitutions tagSubstitutions) {
        var modPriorities = config.getModPriorities();
        var unifyTags = config.getBakedTags();
        var stoneVariantLookup = StoneVariantLookupImpl.create(config.getStoneVariants(), itemTags, blockTags);

        var lookupBuilder = new UnifyLookupImpl.Builder();
        itemTags.forEach((tag, holders) -> {
            if (!unifyTags.contains(tag)) {
                return;
            }

            for (Holder<Item> holder : holders) {
                holder.unwrapKey().ifPresent(key -> {
                    var id = key.location();
                    if (config.includeItem(id)) {
                        lookupBuilder.put(tag, id);
                    }
                });
            }
        });

        return new UnifyHandlerImpl(
                config.getName(),
                modPriorities,
                lookupBuilder.build(modPriorities, stoneVariantLookup, tagSubstitutions),
                config.getIgnoredRecipes(),
                config.getIgnoredRecipeTypes(),
                config.getIgnoredLootTables(),
                config.enableLootUnification(),
                config.shouldHideVariantItems()
        );
    }

    public UnifyHandlerImpl(String name, ModPriorities modPriorities, UnifyLookup unifyLookup, Set<Pattern> ignoredRecipes, Set<Pattern> ignoredRecipeTypes, Set<Pattern> ignoredLootTables, boolean enableLootUnification, boolean recipeViewerHiding) {
        this.name = name;
        this.modPriorities = modPriorities;
        this.ignoredRecipes = ignoredRecipes;
        this.ignoredRecipeTypes = ignoredRecipeTypes;
        this.ignoredLootTables = ignoredLootTables;
        this.enableLootUnification = enableLootUnification;
        this.unifyLookup = unifyLookup;
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
        return unifyLookup.getUnifiedTags();
    }

    @Override
    public Collection<UnificationEntry<Item>> getEntries(TagKey<Item> tag) {
        return unifyLookup.getEntries(tag);
    }

    @Nullable
    @Override
    public UnificationEntry<Item> getEntry(ResourceLocation entry) {
        return unifyLookup.getEntry(entry);
    }

    @Nullable
    @Override
    public UnificationEntry<Item> getEntry(Item item) {
        return unifyLookup.getEntry(item);
    }

    @Nullable
    @Override
    public TagKey<Item> getRelevantItemTag(ResourceLocation item) {
        return unifyLookup.getRelevantItemTag(item);
    }

    @Nullable
    @Override
    public TagKey<Item> getRelevantItemTag(Item item) {
        return unifyLookup.getRelevantItemTag(item);
    }

    @Nullable
    @Override
    public TagKey<Item> getRelevantItemTag(Holder<Item> item) {
        return unifyLookup.getRelevantItemTag(item);
    }

    @Nullable
    @Override
    public UnificationEntry<Item> getItemReplacement(ResourceLocation item) {
        return unifyLookup.getItemReplacement(item);
    }

    @Nullable
    @Override
    public UnificationEntry<Item> getItemReplacement(Item item) {
        return unifyLookup.getItemReplacement(item);
    }

    @Nullable
    @Override
    public UnificationEntry<Item> getItemReplacement(Holder<Item> item) {
        return unifyLookup.getItemReplacement(item);
    }

    @Nullable
    @Override
    public UnificationEntry<Item> getTagTargetItem(TagKey<Item> tag) {
        return unifyLookup.getTagTargetItem(tag);
    }

    @Nullable
    @Override
    public UnificationEntry<Item> getTagTargetItem(TagKey<Item> tag, Predicate<ResourceLocation> itemFilter) {
        return unifyLookup.getTagTargetItem(tag, itemFilter);
    }

    @Override
    public boolean isItemInUnifiedIngredient(Ingredient ingredient, ItemStack item) {
        return unifyLookup.isItemInUnifiedIngredient(ingredient, item);
    }

    @Override
    public TagSubstitutions getTagSubstitutions() {
        return unifyLookup.getTagSubstitutions();
    }

}

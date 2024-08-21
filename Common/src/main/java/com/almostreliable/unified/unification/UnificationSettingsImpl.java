package com.almostreliable.unified.unification;

import com.almostreliable.unified.api.unification.*;
import com.almostreliable.unified.config.UnificationConfig;
import com.almostreliable.unified.utils.VanillaTagWrapper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public final class UnificationSettingsImpl implements UnificationSettings {

    private final String name;
    private final ModPriorities modPriorities;
    private final StoneVariants stoneVariants;
    private final Function<ResourceLocation, Boolean> recipeTypeCheck;
    private final Function<ResourceLocation, Boolean> recipeIdCheck;
    private final boolean recipeViewerHiding;
    private final boolean lootUnification;
    private final Function<ResourceLocation, Boolean> lootTableCheck;
    private final UnificationLookup unificationLookup;
    private final Runnable clearCaches;

    private UnificationSettingsImpl(String name, ModPriorities modPriorities, StoneVariants stoneVariants, Function<ResourceLocation, Boolean> recipeTypeCheck, Function<ResourceLocation, Boolean> recipeIdCheck, boolean recipeViewerHiding, boolean lootUnification, Function<ResourceLocation, Boolean> lootTableCheck, UnificationLookup unificationLookup, Runnable clearCaches) {
        this.name = name;
        this.modPriorities = modPriorities;
        this.stoneVariants = stoneVariants;
        this.recipeTypeCheck = recipeTypeCheck;
        this.recipeIdCheck = recipeIdCheck;
        this.recipeViewerHiding = recipeViewerHiding;
        this.lootUnification = lootUnification;
        this.lootTableCheck = lootTableCheck;
        this.unificationLookup = unificationLookup;
        this.clearCaches = clearCaches;
    }

    public static List<UnificationSettings> create(Collection<UnificationConfig> configs, VanillaTagWrapper<Item> itemTags, VanillaTagWrapper<Block> blockTags, TagSubstitutionsImpl tagSubstitutions) {
        return configs
                .stream()
                .map(config -> create(config, itemTags, blockTags, tagSubstitutions))
                .toList();
    }

    public static UnificationSettings create(UnificationConfig config, VanillaTagWrapper<Item> itemTags, VanillaTagWrapper<Block> blockTags, TagSubstitutions tagSubstitutions) {
        var lookupBuilder = new UnificationLookupImpl.Builder();
        for (var tag : config.getTags()) {
            var itemHolders = itemTags.get(tag);
            for (var itemHolder : itemHolders) {
                itemHolder.unwrapKey().ifPresent(itemKey -> {
                    var itemId = itemKey.location();
                    if (config.shouldIncludeItem(itemId)) {
                        lookupBuilder.put(tag, itemId);
                    }
                });
            }
        }

        ModPriorities modPriorities = config.getModPriorities();
        StoneVariants stoneVariants = StoneVariantsImpl.create(
                config.getStoneVariants(),
                itemTags,
                blockTags
        );

        return new UnificationSettingsImpl(
                config.getName(),
                modPriorities,
                stoneVariants,
                config::shouldIncludeRecipeType,
                config::shouldIncludeRecipeId,
                config.shouldHideVariantItems(),
                config.shouldUnifyLoot(),
                config::shouldIncludeLootTable,
                lookupBuilder.build(modPriorities, stoneVariants, tagSubstitutions),
                config::clearCaches
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
    public StoneVariants getStoneVariants() {
        return stoneVariants;
    }

    @Override
    public boolean shouldIncludeRecipeType(ResourceLocation type) {
        return recipeTypeCheck.apply(type);
    }

    @Override
    public boolean shouldIncludeRecipeId(ResourceLocation id) {
        return recipeIdCheck.apply(id);
    }

    @Override
    public boolean shouldHideVariantItems() {
        return recipeViewerHiding;
    }

    @Override
    public boolean shouldUnifyLoot() {
        return lootUnification;
    }

    @Override
    public boolean shouldIncludeLootTable(ResourceLocation table) {
        return lootTableCheck.apply(table);
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
    public UnificationEntry<Item> getVariantItemTarget(ResourceLocation item) {
        return unificationLookup.getVariantItemTarget(item);
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
        clearCaches.run();
    }
}

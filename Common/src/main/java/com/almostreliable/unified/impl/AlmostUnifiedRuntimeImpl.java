package com.almostreliable.unified.impl;

import com.almostreliable.unified.AlmostUnifiedCommon;
import com.almostreliable.unified.ItemHider;
import com.almostreliable.unified.PluginManager;
import com.almostreliable.unified.api.*;
import com.almostreliable.unified.config.Config;
import com.almostreliable.unified.config.PlaceholderConfig;
import com.almostreliable.unified.config.TagConfig;
import com.almostreliable.unified.config.UnifyConfig;
import com.almostreliable.unified.recipe.RecipeTransformer;
import com.almostreliable.unified.recipe.RecipeUnificationHandler;
import com.almostreliable.unified.recipe.unifier.RecipeUnifierRegistryImpl;
import com.almostreliable.unified.utils.*;
import com.google.gson.JsonElement;
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
import java.util.stream.Collectors;

public final class AlmostUnifiedRuntimeImpl implements AlmostUnifiedRuntime, RecipeUnificationHandler {

    private final Collection<? extends ConfiguredUnificationHandler> configuredUnificationHandlers;
    private final RecipeUnifierRegistry recipeUnifierRegistry;
    private final TagSubstitutions tagSubstitutions;
    private final Placeholders placeholders;
    private final UnificationHandler compositeUnificationHandler;

    private AlmostUnifiedRuntimeImpl(Collection<? extends ConfiguredUnificationHandler> configuredUnificationHandlers, RecipeUnifierRegistry recipeUnifierRegistry, TagSubstitutions tagSubstitutions, Placeholders placeholders) {
        this.configuredUnificationHandlers = configuredUnificationHandlers;
        this.recipeUnifierRegistry = recipeUnifierRegistry;
        this.tagSubstitutions = tagSubstitutions;
        this.placeholders = placeholders;
        this.compositeUnificationHandler = new CompositeUnificationHandler(configuredUnificationHandlers,
                tagSubstitutions);
    }

    public static AlmostUnifiedRuntime create(VanillaTagWrapper<Item> itemTags, VanillaTagWrapper<Block> blockTags) {
        AlmostUnifiedCommon.LOGGER.warn("Reload detected. Reconstructing runtime.");

        FileUtils.createGitIgnore();
        var tagConfig = Config.load(TagConfig.NAME, TagConfig.SERIALIZER);
        var placeholderConfig = Config.load(PlaceholderConfig.NAME, PlaceholderConfig.SERIALIZER);
        var unifyConfigs = UnifyConfig.safeLoadConfigs();

        var allUnifyTags = bakeAndValidateTags(unifyConfigs, itemTags, placeholderConfig);

        RecipeUnifierRegistry recipeUnifierRegistry = new RecipeUnifierRegistryImpl();
        PluginManager.instance().registerRecipeUnifiers(recipeUnifierRegistry);
        // TODO: add plugin support for registering config defaults

        TagReloadHandler.applyCustomTags(tagConfig.getCustomTags(), itemTags);
        TagSubstitutionsImpl tagSubstitutions = TagSubstitutionsImpl.create(
                itemTags::has,
                allUnifyTags::contains,
                tagConfig.getTagSubstitutions()
        );
        tagSubstitutions.apply(itemTags);

        List<ConfiguredUnificationHandler> configuredUnificationHandlers = createAndPrepareUnificationHandlers(
                itemTags,
                blockTags,
                unifyConfigs,
                tagSubstitutions,
                tagConfig.getTagInheritance()
        );
        ItemHider.applyHideTags(itemTags, configuredUnificationHandlers, tagConfig.isEmiHidingStrict());

        return new AlmostUnifiedRuntimeImpl(
                configuredUnificationHandlers,
                recipeUnifierRegistry,
                tagSubstitutions,
                placeholderConfig
        );
    }

    /**
     * Bake all tags from unify configs and validate them.
     * Validating contains:
     * <ul>
     * <li>Tag must exist in vanilla tags, which means that the tag is in used by either vanilla or any mods.</li>
     * <li>Tag must not exist in another unify config. If found, the tag will be skipped.</li>
     * </ul>
     *
     * @param unifyConfigs The unify configs
     * @param itemTags     The vanilla tags
     * @param placeholders The replacements
     * @return The baked tags combined from all unify configs
     */
    private static Set<TagKey<Item>> bakeAndValidateTags(Collection<UnifyConfig> unifyConfigs, VanillaTagWrapper<Item> itemTags, Placeholders placeholders) {
        Set<TagKey<Item>> result = new HashSet<>();

        Map<TagKey<Item>, String> visitedTags = new HashMap<>();
        Set<TagKey<Item>> wrongTags = new HashSet<>();

        for (UnifyConfig config : unifyConfigs) {
            Predicate<TagKey<Item>> validator = tag -> {
                if (!itemTags.has(tag)) {
                    wrongTags.add(tag);
                    return false;
                }

                if (visitedTags.containsKey(tag)) {
                    AlmostUnifiedCommon.LOGGER.warn(
                            "Tag '{}' from unify config '{}' was already created in unify config '{}'",
                            config.getName(),
                            tag.location(),
                            visitedTags.get(tag));
                    return false;
                }

                visitedTags.put(tag, config.getName());
                return true;
            };

            Set<TagKey<Item>> tags = config.bakeTags(validator, placeholders);
            result.addAll(tags);
        }

        if (!wrongTags.isEmpty()) {
            AlmostUnifiedCommon.LOGGER.warn("The following tags are invalid or not in use and will be ignored: {}",
                    wrongTags.stream().map(TagKey::location).collect(Collectors.toList()));
        }

        return result;
    }

    /**
     * Creates all unify handlers for further unification.
     * <p>
     * This method also applies tag inheritance. If tag inheritance was applied, all handlers will be rebuilt due to tag inheritance modifications against vanilla tags.
     *
     * @param itemTags         All existing item tags which are used ingame
     * @param blockTags        All existing block tags which are used ingame
     * @param unifyConfigs     All unify configs
     * @param tagSubstitutions All tag substitutions
     * @param tagInheritance   Tag inheritance data
     * @return All unify handlers
     */
    private static List<ConfiguredUnificationHandler> createAndPrepareUnificationHandlers(VanillaTagWrapper<Item> itemTags, VanillaTagWrapper<Block> blockTags, Collection<UnifyConfig> unifyConfigs, TagSubstitutionsImpl tagSubstitutions, TagInheritance tagInheritance) {
        List<ConfiguredUnificationHandler> configuredUnificationHandlers = ConfiguredUnificationHandlerImpl.create(
                unifyConfigs,
                itemTags,
                blockTags,
                tagSubstitutions);
        var needsRebuild = tagInheritance.apply(itemTags, blockTags, configuredUnificationHandlers);
        if (needsRebuild) {
            configuredUnificationHandlers = ConfiguredUnificationHandlerImpl.create(unifyConfigs,
                    itemTags,
                    blockTags,
                    tagSubstitutions);
        }

        return configuredUnificationHandlers;
    }

    @Override
    public void run(Map<ResourceLocation, JsonElement> recipes) {
        DebugHandler debugHandler = DebugHandler.onRunStart(recipes, compositeUnificationHandler);

        debugHandler.measure(() -> {
            var transformer = new RecipeTransformer(recipeUnifierRegistry, configuredUnificationHandlers);
            return transformer.transformRecipes(recipes);
        });

        debugHandler.onRunEnd(recipes);
    }

    @Override
    public UnificationHandler getUnificationHandler() {
        return compositeUnificationHandler;
    }

    @Override
    public Collection<? extends ConfiguredUnificationHandler> getConfiguredUnificationHandlers() {
        return Collections.unmodifiableCollection(configuredUnificationHandlers);
    }

    @Nullable
    @Override
    public ConfiguredUnificationHandler getUnificationHandler(String name) {
        for (ConfiguredUnificationHandler configuredUnificationHandler : configuredUnificationHandlers) {
            if (configuredUnificationHandler.getName().equals(name)) {
                return configuredUnificationHandler;
            }
        }

        return null;
    }

    @Override
    public TagSubstitutions getTagSubstitutions() {
        return tagSubstitutions;
    }

    @Override
    public Placeholders getPlaceholders() {
        return placeholders;
    }

    private static final class CompositeUnificationHandler implements UnificationHandler {

        private final Iterable<? extends UnificationHandler> unificationHandlers;
        private final TagSubstitutions tagSubstitutions;
        @Nullable private Collection<TagKey<Item>> unifiedTagsView;

        private CompositeUnificationHandler(Iterable<? extends UnificationHandler> unificationHandlers, TagSubstitutions tagSubstitutions) {
            this.unificationHandlers = unificationHandlers;
            this.tagSubstitutions = tagSubstitutions;
        }

        @Override
        public Collection<TagKey<Item>> getUnifiedTags() {
            if (unifiedTagsView == null) {
                Collection<Collection<TagKey<Item>>> iterables = new ArrayList<>();
                for (var unificationHandler : unificationHandlers) {
                    iterables.add(unificationHandler.getUnifiedTags());
                }

                unifiedTagsView = new CompositeCollection<>(iterables);
            }

            return unifiedTagsView;
        }

        @Override
        public Collection<UnificationEntry<Item>> getEntries(TagKey<Item> tag) {
            for (var unificationHandler : unificationHandlers) {
                var resultItems = unificationHandler.getEntries(tag);
                if (!resultItems.isEmpty()) {
                    return resultItems;
                }
            }

            return Collections.emptyList();
        }

        @Nullable
        @Override
        public UnificationEntry<Item> getEntry(ResourceLocation entry) {
            for (var unificationHandler : unificationHandlers) {
                var resultItem = unificationHandler.getEntry(entry);
                if (resultItem != null) {
                    return resultItem;
                }
            }

            return null;
        }

        @Nullable
        @Override
        public UnificationEntry<Item> getEntry(Item item) {
            for (var unificationHandler : unificationHandlers) {
                var resultItem = unificationHandler.getEntry(item);
                if (resultItem != null) {
                    return resultItem;
                }
            }

            return null;
        }

        @Nullable
        @Override
        public TagKey<Item> getRelevantItemTag(ResourceLocation item) {
            for (var unificationHandler : unificationHandlers) {
                TagKey<Item> tag = unificationHandler.getRelevantItemTag(item);
                if (tag != null) {
                    return tag;
                }
            }

            return null;
        }

        @Nullable
        @Override
        public TagKey<Item> getRelevantItemTag(Item item) {
            for (var unificationHandler : unificationHandlers) {
                TagKey<Item> tag = unificationHandler.getRelevantItemTag(item);
                if (tag != null) {
                    return tag;
                }
            }

            return null;
        }

        @Nullable
        @Override
        public TagKey<Item> getRelevantItemTag(Holder<Item> item) {
            for (var unificationHandler : unificationHandlers) {
                TagKey<Item> tag = unificationHandler.getRelevantItemTag(item);
                if (tag != null) {
                    return tag;
                }
            }

            return null;
        }

        @Override
        public UnificationEntry<Item> getItemReplacement(ResourceLocation item) {
            for (var unificationHandler : unificationHandlers) {
                var resultItem = unificationHandler.getItemReplacement(item);
                if (resultItem != null) {
                    return resultItem;
                }
            }

            return null;
        }

        @Override
        public UnificationEntry<Item> getItemReplacement(Item item) {
            for (var unificationHandler : unificationHandlers) {
                var resultItem = unificationHandler.getItemReplacement(item);
                if (resultItem != null) {
                    return resultItem;
                }
            }

            return null;
        }

        @Override
        public UnificationEntry<Item> getItemReplacement(Holder<Item> item) {
            for (var unificationHandler : unificationHandlers) {
                var resultItem = unificationHandler.getItemReplacement(item);
                if (resultItem != null) {
                    return resultItem;
                }
            }

            return null;
        }

        @Override
        public UnificationEntry<Item> getTagTargetItem(TagKey<Item> tag) {
            for (var unificationHandler : unificationHandlers) {
                var result = unificationHandler.getTagTargetItem(tag);
                if (result != null) {
                    return result;
                }
            }

            return null;
        }

        @Override
        public UnificationEntry<Item> getTagTargetItem(TagKey<Item> tag, Predicate<ResourceLocation> itemFilter) {
            for (var unificationHandler : unificationHandlers) {
                var result = unificationHandler.getTagTargetItem(tag, itemFilter);
                if (result != null) {
                    return result;
                }
            }

            return null;
        }

        @Override
        public boolean isItemInUnifiedIngredient(Ingredient ingredient, ItemStack item) {
            for (var unificationHandler : unificationHandlers) {
                if (unificationHandler.isItemInUnifiedIngredient(ingredient, item)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public TagSubstitutions getTagSubstitutions() {
            return tagSubstitutions;
        }
    }
}

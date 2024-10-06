package com.almostreliable.unified.core;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import com.almostreliable.unified.AlmostUnifiedCommon;
import com.almostreliable.unified.api.AlmostUnifiedRuntime;
import com.almostreliable.unified.api.unification.Placeholders;
import com.almostreliable.unified.api.unification.TagSubstitutions;
import com.almostreliable.unified.api.unification.UnificationEntry;
import com.almostreliable.unified.api.unification.UnificationLookup;
import com.almostreliable.unified.api.unification.UnificationSettings;
import com.almostreliable.unified.api.unification.recipe.CustomIngredientUnifierRegistry;
import com.almostreliable.unified.api.unification.recipe.RecipeUnifierRegistry;
import com.almostreliable.unified.compat.PluginManager;
import com.almostreliable.unified.compat.viewer.ItemHider;
import com.almostreliable.unified.config.Config;
import com.almostreliable.unified.config.DebugConfig;
import com.almostreliable.unified.config.PlaceholderConfig;
import com.almostreliable.unified.config.TagConfig;
import com.almostreliable.unified.config.UnificationConfig;
import com.almostreliable.unified.unification.TagInheritance;
import com.almostreliable.unified.unification.TagSubstitutionsImpl;
import com.almostreliable.unified.unification.UnificationSettingsImpl;
import com.almostreliable.unified.unification.recipe.CustomIngredientUnifierRegistryImpl;
import com.almostreliable.unified.unification.recipe.RecipeTransformer;
import com.almostreliable.unified.unification.recipe.RecipeUnifierRegistryImpl;
import com.almostreliable.unified.utils.DebugHandler;
import com.almostreliable.unified.utils.FileUtils;
import com.almostreliable.unified.utils.VanillaTagWrapper;

import com.google.gson.JsonElement;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class AlmostUnifiedRuntimeImpl implements AlmostUnifiedRuntime {

    private final Collection<? extends UnificationSettings> unificationSettings;
    private final CustomIngredientUnifierRegistry ingredientUnifierRegistry;
    private final RecipeUnifierRegistry recipeUnifierRegistry;
    private final TagSubstitutions tagSubstitutions;
    private final Placeholders placeholders;
    private final UnificationLookup compositeUnificationLookup;
    private final DebugHandler debugHandler;

    private AlmostUnifiedRuntimeImpl(Collection<? extends UnificationSettings> unificationSettings, CustomIngredientUnifierRegistry ingredientUnifierRegistry, RecipeUnifierRegistry recipeUnifierRegistry, TagSubstitutions tagSubstitutions, Placeholders placeholders, DebugConfig debugConfig) {
        this.unificationSettings = unificationSettings;
        this.ingredientUnifierRegistry = ingredientUnifierRegistry;
        this.recipeUnifierRegistry = recipeUnifierRegistry;
        this.tagSubstitutions = tagSubstitutions;
        this.placeholders = placeholders;
        this.compositeUnificationLookup = new CompositeUnificationLookup(unificationSettings);
        this.debugHandler = new DebugHandler(debugConfig);
    }

    public static AlmostUnifiedRuntimeImpl create(VanillaTagWrapper<Item> itemTags, VanillaTagWrapper<Block> blockTags) {
        AlmostUnifiedCommon.LOGGER.warn("Reload detected. Reconstructing runtime.");

        FileUtils.createGitIgnore();
        var debugConfig = Config.load(DebugConfig.NAME, DebugConfig.SERIALIZER);
        var placeholderConfig = Config.load(PlaceholderConfig.NAME, PlaceholderConfig.SERIALIZER);
        var tagConfig = Config.load(TagConfig.NAME, TagConfig.SERIALIZER);
        var unificationConfigs = UnificationConfig.safeLoadConfigs();

        var unificationTags = bakeAndValidateTags(
            unificationConfigs,
            itemTags,
            placeholderConfig,
            debugConfig.shouldLogInvalidTags()
        );

        CustomIngredientUnifierRegistry ingredientUnifierRegistry = new CustomIngredientUnifierRegistryImpl();
        PluginManager.instance().registerCustomIngredientUnifiers(ingredientUnifierRegistry);
        RecipeUnifierRegistry recipeUnifierRegistry = new RecipeUnifierRegistryImpl();
        PluginManager.instance().registerRecipeUnifiers(recipeUnifierRegistry);
        // TODO: add plugin support for registering config defaults

        TagReloadHandler.applyCustomTags(tagConfig.getCustomTags(), itemTags);
        TagSubstitutionsImpl tagSubstitutions = TagSubstitutionsImpl.create(
            itemTags::has,
            unificationTags::contains,
            tagConfig.getTagSubstitutions()
        );
        tagSubstitutions.apply(itemTags);

        List<UnificationSettings> unificationSettings = createUnificationLookups(
            itemTags,
            blockTags,
            unificationConfigs,
            tagSubstitutions,
            tagConfig.getTagInheritance()
        );
        ItemHider.applyHideTags(itemTags, unificationSettings, tagConfig.isEmiHidingStrict());

        return new AlmostUnifiedRuntimeImpl(
            unificationSettings,
            ingredientUnifierRegistry,
            recipeUnifierRegistry,
            tagSubstitutions,
            placeholderConfig,
            debugConfig
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
     * @param unificationConfigs The unify configs
     * @param itemTags           The vanilla tags
     * @param placeholders       The replacements
     * @param logInvalidTags     Whether to log invalid tags
     * @return The baked tags combined from all unify configs
     */
    private static Set<TagKey<Item>> bakeAndValidateTags(Collection<UnificationConfig> unificationConfigs, VanillaTagWrapper<Item> itemTags, Placeholders placeholders, boolean logInvalidTags) {
        Set<TagKey<Item>> result = new HashSet<>();

        Map<TagKey<Item>, String> visitedTags = new HashMap<>();
        Set<TagKey<Item>> wrongTags = new HashSet<>();

        for (UnificationConfig config : unificationConfigs) {
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

        if (!wrongTags.isEmpty() && logInvalidTags) {
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
     * @param itemTags           All existing item tags which are used ingame
     * @param blockTags          All existing block tags which are used ingame
     * @param unificationConfigs All unify configs
     * @param tagSubstitutions   All tag substitutions
     * @param tagInheritance     Tag inheritance data
     * @return All unify handlers
     */
    private static List<UnificationSettings> createUnificationLookups(VanillaTagWrapper<Item> itemTags, VanillaTagWrapper<Block> blockTags, Collection<UnificationConfig> unificationConfigs, TagSubstitutionsImpl tagSubstitutions, TagInheritance tagInheritance) {
        var unificationSettings = UnificationSettingsImpl.create(unificationConfigs,
            itemTags,
            blockTags,
            tagSubstitutions);

        var needsRebuild = tagInheritance.apply(itemTags, blockTags, unificationSettings);
        if (needsRebuild) {
            return UnificationSettingsImpl.create(unificationConfigs, itemTags, blockTags, tagSubstitutions);
        }

        return unificationSettings;
    }

    public void run(Map<ResourceLocation, JsonElement> recipes) {
        debugHandler.onRunStart(recipes, compositeUnificationLookup);

        debugHandler.measure(() ->
            new RecipeTransformer(ingredientUnifierRegistry, recipeUnifierRegistry, unificationSettings)
                .transformRecipes(recipes)
        );

        debugHandler.onRunEnd(recipes);
    }

    @Override
    public UnificationLookup getUnificationLookup() {
        return compositeUnificationLookup;
    }

    @Override
    public Collection<? extends UnificationSettings> getUnificationSettings() {
        return Collections.unmodifiableCollection(unificationSettings);
    }

    @Nullable
    @Override
    public UnificationSettings getUnificationSettings(String name) {
        for (UnificationSettings settings : unificationSettings) {
            if (settings.getName().equals(name)) {
                return settings;
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

    public DebugHandler getDebugHandler() {
        return debugHandler;
    }

    private static final class CompositeUnificationLookup implements UnificationLookup {

        private final Iterable<? extends UnificationLookup> unificationLookups;

        @Nullable private Collection<TagKey<Item>> unificationTagsView;

        private CompositeUnificationLookup(Iterable<? extends UnificationLookup> unificationLookups) {
            this.unificationLookups = unificationLookups;
        }

        @Override
        public Collection<TagKey<Item>> getTags() {
            if (unificationTagsView == null) {
                Set<TagKey<Item>> tagView = new HashSet<>();
                for (var unificationLookup : unificationLookups) {
                    tagView.addAll(unificationLookup.getTags());
                }

                unificationTagsView = Collections.unmodifiableCollection(tagView);
            }

            return unificationTagsView;
        }

        @Override
        public Collection<UnificationEntry<Item>> getTagEntries(TagKey<Item> tag) {
            for (var unificationLookup : unificationLookups) {
                var resultItems = unificationLookup.getTagEntries(tag);
                if (!resultItems.isEmpty()) {
                    return resultItems;
                }
            }

            return Collections.emptyList();
        }

        @Nullable
        @Override
        public UnificationEntry<Item> getItemEntry(ResourceLocation item) {
            for (var unificationLookup : unificationLookups) {
                var resultItem = unificationLookup.getItemEntry(item);
                if (resultItem != null) {
                    return resultItem;
                }
            }

            return null;
        }

        @Nullable
        @Override
        public TagKey<Item> getRelevantItemTag(ResourceLocation item) {
            for (var unificationLookup : unificationLookups) {
                TagKey<Item> tag = unificationLookup.getRelevantItemTag(item);
                if (tag != null) {
                    return tag;
                }
            }

            return null;
        }

        @Override
        public UnificationEntry<Item> getVariantItemTarget(ResourceLocation item) {
            for (var unificationLookup : unificationLookups) {
                var resultItem = unificationLookup.getVariantItemTarget(item);
                if (resultItem != null) {
                    return resultItem;
                }
            }

            return null;
        }

        @Override
        public UnificationEntry<Item> getTagTargetItem(TagKey<Item> tag, Predicate<ResourceLocation> itemFilter) {
            for (var unificationLookup : unificationLookups) {
                var result = unificationLookup.getTagTargetItem(tag, itemFilter);
                if (result != null) {
                    return result;
                }
            }

            return null;
        }

        @Override
        public boolean isUnifiedIngredientItem(Ingredient ingredient, ItemStack item) {
            for (var unificationLookup : unificationLookups) {
                if (unificationLookup.isUnifiedIngredientItem(ingredient, item)) {
                    return true;
                }
            }

            return false;
        }
    }
}

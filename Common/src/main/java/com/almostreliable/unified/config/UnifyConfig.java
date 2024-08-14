package com.almostreliable.unified.config;

import com.almostreliable.unified.AlmostUnifiedCommon;
import com.almostreliable.unified.AlmostUnifiedPlatform;
import com.almostreliable.unified.api.ModPriorities;
import com.almostreliable.unified.api.Placeholders;
import com.almostreliable.unified.recipe.ModPrioritiesImpl;
import com.almostreliable.unified.utils.JsonUtils;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class UnifyConfig extends Config {

    private static final String SUB_FOLDER = "unification";

    private final List<String> modPriorities;
    private final Map<TagKey<Item>, String> priorityOverrides;
    private final List<String> stoneVariants;
    private final List<String> unbakedTags;
    private final Set<TagKey<Item>> ignoredTags;
    private final Set<Pattern> ignoredItems;
    private final Set<Pattern> ignoredRecipeTypes;
    private final Set<Pattern> ignoredRecipes;
    private final Set<Pattern> ignoredLootTables;
    private final boolean enableLootUnification;
    private final boolean recipeViewerHiding;
    @Nullable private Set<TagKey<Item>> bakedTagsCache;

    private UnifyConfig(String name, List<String> modPriorities, Map<TagKey<Item>, String> priorityOverrides, List<String> stoneVariants, List<String> unbakedTags, Set<TagKey<Item>> ignoredTags, Set<Pattern> ignoredItems, Set<Pattern> ignoredRecipeTypes, Set<Pattern> ignoredRecipes, Set<Pattern> ignoredLootTables, boolean enableLootUnification, boolean recipeViewerHiding) {
        super(name);
        this.modPriorities = modPriorities;
        this.priorityOverrides = priorityOverrides;
        this.stoneVariants = stoneVariants;
        this.unbakedTags = unbakedTags;
        this.ignoredTags = ignoredTags;
        this.ignoredItems = ignoredItems;
        this.ignoredRecipeTypes = ignoredRecipeTypes;
        this.ignoredRecipes = ignoredRecipes;
        this.ignoredLootTables = ignoredLootTables;
        this.enableLootUnification = enableLootUnification;
        this.recipeViewerHiding = recipeViewerHiding;
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static Collection<UnifyConfig> safeLoadConfigs() {
        try {
            return loadConfigs();
        } catch (Exception e) {
            AlmostUnifiedCommon.LOGGER.error("Could not load unify configs.", e);
            return List.of();
        }
    }

    private static Collection<UnifyConfig> loadConfigs() throws IOException {
        Path subFolder = createConfigDir().resolve(SUB_FOLDER);

        var jsons = readJsons(subFolder);
        if (jsons.isEmpty()) {
            String name = "materials";
            UnifySerializer serializer = new UnifySerializer(name);
            UnifyConfig defaultConfig = serializer.deserialize(new JsonObject());
            save(subFolder.resolve(name + ".json"), defaultConfig, serializer);
            return List.of(defaultConfig);
        }

        Collection<UnifyConfig> configs = new ArrayList<>();
        for (var entry : jsons.entrySet()) {
            String name = entry.getKey();
            JsonObject json = entry.getValue();

            UnifySerializer serializer = new UnifySerializer(name);
            var config = serializer.deserialize(json);
            if (serializer.isInvalid()) {
                save(subFolder.resolve(name + ".json"), config, serializer);
            }

            configs.add(config);
        }

        logMissingPriorityMods(configs);
        return configs;
    }

    private static Map<String, JsonObject> readJsons(Path subFolder) throws IOException {
        Files.createDirectories(subFolder);
        var files = FileUtils.listFiles(subFolder.toFile(), new String[]{ "json" }, false);

        Map<String, JsonObject> jsons = new HashMap<>();
        for (var file : files) {
            String fileName = FilenameUtils.getBaseName(file.getName());
            try {
                jsons.put(fileName, JsonUtils.readFromFile(file.toPath(), JsonObject.class));
            } catch (Throwable e) {
                AlmostUnifiedCommon.LOGGER.error("Unify config '{}.json' could not be loaded.", fileName, e);
            }
        }

        return jsons;
    }

    private static void logMissingPriorityMods(Collection<UnifyConfig> unifyConfigs) {
        Set<String> mods = unifyConfigs
                .stream()
                .map(UnifyConfig::getModPriorities)
                .flatMap(ModPriorities::stream)
                .filter(m -> !AlmostUnifiedPlatform.INSTANCE.isModLoaded(m))
                .collect(Collectors.toSet());

        if (mods.isEmpty()) return;
        AlmostUnifiedCommon.LOGGER.warn("The following mods are used in unification settings, but are not loaded: {}",
                mods);
    }

    public ModPriorities getModPriorities() {
        return new ModPrioritiesImpl(modPriorities, priorityOverrides);
    }

    public List<String> getStoneVariants() {
        return stoneVariants;
    }

    public Set<TagKey<Item>> getBakedTags() {
        if (bakedTagsCache == null) {
            throw new IllegalStateException("Tags are not baked. bakeTags(...) must be called first");
        }

        return bakedTagsCache;
    }

    public Set<TagKey<Item>> bakeTags(Predicate<TagKey<Item>> tagValidator, Placeholders placeholders) {
        if (bakedTagsCache != null) {
            return bakedTagsCache;
        }

        Set<TagKey<Item>> result = new HashSet<>();
        for (var unbakedTag : unbakedTags) {
            var inflate = placeholders.inflate(unbakedTag);
            for (var rl : inflate) {
                var tag = TagKey.create(Registries.ITEM, rl);
                if (ignoredTags.contains(tag)) continue;
                if (result.contains(tag)) continue;
                if (!tagValidator.test(tag)) continue;

                result.add(tag);
            }
        }

        bakedTagsCache = result;
        return result;
    }

    public boolean includeItem(ResourceLocation item) {
        for (Pattern pattern : ignoredItems) {
            if (pattern.matcher(item.toString()).matches()) {
                return false;
            }
        }
        return true;
    }

    public Set<Pattern> getIgnoredRecipeTypes() {
        return ignoredRecipeTypes;
    }

    public Set<Pattern> getIgnoredRecipes() {
        return ignoredRecipes;
    }

    public Set<Pattern> getIgnoredLootTables() {
        return ignoredLootTables;
    }

    public boolean enableLootUnification() {
        return enableLootUnification;
    }

    public boolean shouldHideVariantItems() {
        return recipeViewerHiding;
    }

    public static final class UnifySerializer extends Config.Serializer<UnifyConfig> {

        private static final String MOD_PRIORITIES = "mod_priorities";
        private static final String STONE_VARIANTS = "stone_variants";
        private static final String TAGS = "tags";
        private static final String PRIORITY_OVERRIDES = "priority_overrides";
        private static final String IGNORED_TAGS = "ignored_tags";
        private static final String IGNORED_ITEMS = "ignored_items";
        private static final String IGNORED_RECIPE_TYPES = "ignored_recipe_types";
        private static final String IGNORED_RECIPES = "ignored_recipes";
        private static final String IGNORED_LOOT_TABLES = "ignored_loot_tables";
        private static final String RECIPE_VIEWER_HIDING = "recipe_viewer_hiding";
        private static final String ENABLE_LOOT_UNIFICATION = "enable_loot_unification";

        private final String name;

        private UnifySerializer(String name) {
            this.name = name;
        }

        @Override
        public UnifyConfig handleDeserialization(JsonObject json) {
            var platform = AlmostUnifiedPlatform.INSTANCE.getPlatform();

            // Mod priorities
            List<String> modPriorities = safeGet(() -> JsonUtils.toList(json.getAsJsonArray(MOD_PRIORITIES)),
                    Defaults.MOD_PRIORITIES);

            Map<TagKey<Item>, String> priorityOverrides = safeGet(() -> JsonUtils.deserializeMap(json,
                    PRIORITY_OVERRIDES,
                    e -> TagKey.create(Registries.ITEM, ResourceLocation.parse(e.getKey())),
                    e -> e.getValue().getAsString()), new HashMap<>());

            List<String> stoneVariants = safeGet(() -> JsonUtils.toList(json.getAsJsonArray(STONE_VARIANTS)),
                    Defaults.STONE_VARIANTS);
            List<String> tags = safeGet(() -> JsonUtils.toList(json.getAsJsonArray(TAGS)), Defaults.TAGS);

            Set<TagKey<Item>> ignoredTags = safeGet(() -> JsonUtils
                    .toList(json.getAsJsonArray(IGNORED_TAGS))
                    .stream()
                    .map(s -> TagKey.create(Registries.ITEM, ResourceLocation.parse(s)))
                    .collect(Collectors.toSet()), new HashSet<>());
            Set<Pattern> ignoredItems = deserializePatterns(json, IGNORED_ITEMS, List.of());
            Set<Pattern> ignoredRecipeTypes = deserializePatterns(
                    json,
                    IGNORED_RECIPE_TYPES,
                    Defaults.IGNORED_RECIPE_TYPES
            );
            Set<Pattern> ignoredRecipes = deserializePatterns(json, IGNORED_RECIPES, List.of());
            Set<Pattern> ignoredLootTables = deserializePatterns(json, IGNORED_LOOT_TABLES, List.of());
            boolean enableLootUnification = safeGet(() -> json
                    .getAsJsonPrimitive(ENABLE_LOOT_UNIFICATION)
                    .getAsBoolean(), false);
            boolean recipeViewerHiding = safeGet(() -> json.getAsJsonPrimitive(RECIPE_VIEWER_HIDING).getAsBoolean(),
                    true);

            return new UnifyConfig(
                    name,
                    modPriorities,
                    priorityOverrides,
                    stoneVariants,
                    tags,
                    ignoredTags,
                    ignoredItems,
                    ignoredRecipeTypes,
                    ignoredRecipes,
                    ignoredLootTables,
                    enableLootUnification,
                    recipeViewerHiding
            );
        }

        @Override
        public JsonObject serialize(UnifyConfig config) {
            JsonObject json = new JsonObject();
            json.add(MOD_PRIORITIES, JsonUtils.toArray(config.modPriorities));
            json.add(STONE_VARIANTS, JsonUtils.toArray(config.stoneVariants));
            json.add(TAGS, JsonUtils.toArray(config.unbakedTags));

            JsonObject priorityOverrides = new JsonObject();
            config.priorityOverrides.forEach((tag, mod) -> {
                priorityOverrides.addProperty(tag.location().toString(), mod);
            });
            json.add(PRIORITY_OVERRIDES, priorityOverrides);

            json.add(IGNORED_TAGS,
                    JsonUtils.toArray(config.ignoredTags
                            .stream()
                            .map(TagKey::location)
                            .map(ResourceLocation::toString)
                            .toList()));
            serializePatterns(json, IGNORED_ITEMS, config.ignoredItems);
            serializePatterns(json, IGNORED_RECIPE_TYPES, config.ignoredRecipeTypes);
            serializePatterns(json, IGNORED_RECIPES, config.ignoredRecipes);
            serializePatterns(json, IGNORED_LOOT_TABLES, config.ignoredLootTables);
            json.addProperty(ENABLE_LOOT_UNIFICATION, config.enableLootUnification);
            json.addProperty(RECIPE_VIEWER_HIDING, config.recipeViewerHiding);
            return json;
        }
    }
}

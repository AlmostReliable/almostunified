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

public final class UnificationConfig extends Config {

    private static final String SUB_FOLDER = "unification";

    private final List<String> modPriorities;
    private final Map<TagKey<Item>, String> priorityOverrides;
    private final List<String> stoneVariants;
    private final List<String> tags;
    private final Set<TagKey<Item>> ignoredTags;
    private final Set<Pattern> ignoredItems;
    private final Set<Pattern> ignoredRecipeTypes;
    private final Set<Pattern> ignoredRecipeIds;
    private final boolean recipeViewerHiding;
    private final boolean lootUnification;
    private final Set<Pattern> ignoredLootTables;

    private final Map<ResourceLocation, Boolean> ignoredItemsCache = new HashMap<>();
    private final Map<ResourceLocation, Boolean> ignoredRecipeTypesCache = new HashMap<>();
    private final Map<ResourceLocation, Boolean> ignoredRecipeIdsCache = new HashMap<>();
    private final Map<ResourceLocation, Boolean> ignoredLootTablesCache = new HashMap<>();
    @Nullable private Set<TagKey<Item>> bakedTags;

    private UnificationConfig(String name, List<String> modPriorities, Map<TagKey<Item>, String> priorityOverrides, List<String> stoneVariants, List<String> tags, Set<TagKey<Item>> ignoredTags, Set<Pattern> ignoredItems, Set<Pattern> ignoredRecipeTypes, Set<Pattern> ignoredRecipeIds, boolean recipeViewerHiding, boolean lootUnification, Set<Pattern> ignoredLootTables) {
        super(name);
        this.modPriorities = modPriorities;
        this.priorityOverrides = priorityOverrides;
        this.stoneVariants = stoneVariants;
        this.tags = tags;
        this.ignoredTags = ignoredTags;
        this.ignoredItems = ignoredItems;
        this.ignoredRecipeTypes = ignoredRecipeTypes;
        this.ignoredRecipeIds = ignoredRecipeIds;
        this.recipeViewerHiding = recipeViewerHiding;
        this.lootUnification = lootUnification;
        this.ignoredLootTables = ignoredLootTables;
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static Collection<UnificationConfig> safeLoadConfigs() {
        try {
            return loadConfigs();
        } catch (Exception e) {
            AlmostUnifiedCommon.LOGGER.error("Could not load unify configs.", e);
            return List.of();
        }
    }

    private static Collection<UnificationConfig> loadConfigs() throws IOException {
        Path subFolder = createConfigDir().resolve(SUB_FOLDER);

        var jsons = readJsons(subFolder);
        if (jsons.isEmpty()) {
            String name = "materials";
            UnifySerializer serializer = new UnifySerializer(name);
            UnificationConfig defaultConfig = serializer.deserialize(new JsonObject());
            save(subFolder.resolve(name + ".json"), defaultConfig, serializer);
            return List.of(defaultConfig);
        }

        Collection<UnificationConfig> configs = new ArrayList<>();
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

    private static void logMissingPriorityMods(Collection<UnificationConfig> unificationConfigs) {
        Set<String> mods = unificationConfigs
                .stream()
                .map(UnificationConfig::getModPriorities)
                .flatMap(ModPriorities::stream)
                .filter(m -> !AlmostUnifiedPlatform.INSTANCE.isModLoaded(m))
                .collect(Collectors.toSet());

        if (mods.isEmpty()) return;
        AlmostUnifiedCommon.LOGGER.warn(
                "The following mods are used in unification settings, but are not loaded: {}",
                mods
        );
    }

    public ModPriorities getModPriorities() {
        return new ModPrioritiesImpl(modPriorities, priorityOverrides);
    }

    public List<String> getStoneVariants() {
        return stoneVariants;
    }

    public Set<TagKey<Item>> getTags() {
        if (bakedTags == null) {
            throw new IllegalStateException("unification tags are not baked yet");
        }

        return bakedTags;
    }

    public Set<TagKey<Item>> bakeTags(Predicate<TagKey<Item>> tagValidator, Placeholders placeholders) {
        if (bakedTags != null) return bakedTags;

        bakedTags = new HashSet<>();
        for (var tag : tags) {
            var replacedTags = placeholders.apply(tag);
            for (var replacedTag : replacedTags) {
                ResourceLocation parsedTag = ResourceLocation.tryParse(replacedTag);
                if (parsedTag == null) continue;

                var tagKey = TagKey.create(Registries.ITEM, parsedTag);
                if (ignoredTags.contains(tagKey)) continue;
                if (!tagValidator.test(tagKey)) continue;

                bakedTags.add(tagKey);
            }
        }

        return bakedTags;
    }

    public boolean shouldIncludeItem(ResourceLocation item) {
        return ignoredItemsCache.computeIfAbsent(item, i -> {
            for (Pattern pattern : ignoredItems) {
                if (pattern.matcher(i.toString()).matches()) {
                    return false;
                }
            }

            return true;
        });
    }

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

    public boolean shouldIncludeRecipeId(ResourceLocation id) {
        return ignoredRecipeIdsCache.computeIfAbsent(id, i -> {
            for (Pattern pattern : ignoredRecipeIds) {
                if (pattern.matcher(i.toString()).matches()) {
                    return false;
                }
            }

            return true;
        });
    }

    public boolean shouldHideVariantItems() {
        return recipeViewerHiding;
    }

    public boolean shouldUnifyLoot() {
        return lootUnification;
    }

    public boolean shouldIncludeLootTable(ResourceLocation table) {
        return ignoredLootTablesCache.computeIfAbsent(table, t -> {
            for (Pattern pattern : ignoredRecipeIds) {
                if (pattern.matcher(t.toString()).matches()) {
                    return false;
                }
            }

            return true;
        });
    }

    public void clearCaches() {
        ignoredItemsCache.clear();
        ignoredRecipeTypesCache.clear();
        ignoredRecipeIdsCache.clear();
        ignoredLootTablesCache.clear();
    }

    public static final class UnifySerializer extends Config.Serializer<UnificationConfig> {

        private static final String MOD_PRIORITIES = "mod_priorities";
        private static final String PRIORITY_OVERRIDES = "priority_overrides";
        private static final String STONE_VARIANTS = "stone_variants";
        private static final String TAGS = "tags";
        private static final String IGNORED_TAGS = "ignored_tags";
        private static final String IGNORED_ITEMS = "ignored_items";
        private static final String IGNORED_RECIPE_TYPES = "ignored_recipe_types";
        private static final String IGNORED_RECIPES_IDS = "ignored_recipe_ids";
        private static final String RECIPE_VIEWER_HIDING = "recipe_viewer_hiding";
        private static final String LOOT_UNIFICATION = "loot_unification";
        private static final String IGNORED_LOOT_TABLES = "ignored_loot_tables";

        private final String name;

        private UnifySerializer(String name) {
            this.name = name;
        }

        @Override
        public UnificationConfig handleDeserialization(JsonObject json) {
            List<String> modPriorities = safeGet(
                    () -> JsonUtils.toList(json.getAsJsonArray(MOD_PRIORITIES)),
                    Defaults.MOD_PRIORITIES
            );
            Map<TagKey<Item>, String> priorityOverrides = safeGet(
                    () -> JsonUtils.deserializeMap(
                            json,
                            PRIORITY_OVERRIDES,
                            e -> TagKey.create(Registries.ITEM, ResourceLocation.parse(e.getKey())),
                            e -> e.getValue().getAsString()
                    ),
                    new HashMap<>()
            );

            List<String> stoneVariants = safeGet(
                    () -> JsonUtils.toList(json.getAsJsonArray(STONE_VARIANTS)),
                    Defaults.STONE_VARIANTS
            );

            List<String> tags = safeGet(() -> JsonUtils.toList(json.getAsJsonArray(TAGS)), Defaults.TAGS);
            Set<TagKey<Item>> ignoredTags = safeGet(
                    () -> JsonUtils
                            .toList(json.getAsJsonArray(IGNORED_TAGS))
                            .stream()
                            .map(s -> TagKey.create(Registries.ITEM, ResourceLocation.parse(s)))
                            .collect(Collectors.toSet()),
                    new HashSet<>()
            );
            Set<Pattern> ignoredItems = deserializePatterns(json, IGNORED_ITEMS, List.of());
            Set<Pattern> ignoredRecipeTypes = deserializePatterns(
                    json,
                    IGNORED_RECIPE_TYPES,
                    Defaults.IGNORED_RECIPE_TYPES
            );
            Set<Pattern> ignoredRecipeIds = deserializePatterns(json, IGNORED_RECIPES_IDS, List.of());

            boolean recipeViewerHiding = safeGet(
                    () -> json.getAsJsonPrimitive(RECIPE_VIEWER_HIDING).getAsBoolean(),
                    true
            );

            boolean lootUnification = safeGet(
                    () -> json.getAsJsonPrimitive(LOOT_UNIFICATION).getAsBoolean(),
                    false
            );
            Set<Pattern> ignoredLootTables = deserializePatterns(json, IGNORED_LOOT_TABLES, List.of());

            return new UnificationConfig(
                    name,
                    modPriorities,
                    priorityOverrides,
                    stoneVariants,
                    tags,
                    ignoredTags,
                    ignoredItems,
                    ignoredRecipeTypes,
                    ignoredRecipeIds,
                    recipeViewerHiding,
                    lootUnification,
                    ignoredLootTables
            );
        }

        @Override
        public JsonObject serialize(UnificationConfig config) {
            JsonObject json = new JsonObject();

            json.add(MOD_PRIORITIES, JsonUtils.toArray(config.modPriorities));
            JsonObject priorityOverrides = new JsonObject();
            config.priorityOverrides.forEach(
                    (tag, mod) -> priorityOverrides.addProperty(tag.location().toString(), mod)
            );
            json.add(PRIORITY_OVERRIDES, priorityOverrides);

            json.add(STONE_VARIANTS, JsonUtils.toArray(config.stoneVariants));

            json.add(TAGS, JsonUtils.toArray(config.tags));
            json.add(
                    IGNORED_TAGS,
                    JsonUtils.toArray(config.ignoredTags
                            .stream()
                            .map(TagKey::location)
                            .map(ResourceLocation::toString)
                            .toList())
            );
            serializePatterns(json, IGNORED_ITEMS, config.ignoredItems);
            serializePatterns(json, IGNORED_RECIPE_TYPES, config.ignoredRecipeTypes);
            serializePatterns(json, IGNORED_RECIPES_IDS, config.ignoredRecipeIds);

            json.addProperty(RECIPE_VIEWER_HIDING, config.recipeViewerHiding);

            json.addProperty(LOOT_UNIFICATION, config.lootUnification);
            serializePatterns(json, IGNORED_LOOT_TABLES, config.ignoredLootTables);

            return json;
        }
    }
}

package com.almostreliable.unified.config;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.AlmostUnifiedPlatform;
import com.almostreliable.unified.api.ModPriorities;
import com.almostreliable.unified.api.Placeholders;
import com.almostreliable.unified.recipe.ModPrioritiesImpl;
import com.almostreliable.unified.utils.JsonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UnifyConfig extends Config {

    public static final String ENABLE_LOOT_UNIFICATION = "enableLootUnification";
    private final List<String> modPriorities;
    private final Map<TagKey<Item>, String> priorityOverrides;
    private final List<String> stoneStrata;
    private final List<String> unbakedTags;
    private final Set<TagKey<Item>> ignoredTags;
    private final Set<Pattern> ignoredItems;
    private final Set<Pattern> ignoredRecipeTypes;
    private final Set<Pattern> ignoredRecipes;
    private final Set<Pattern> ignoredLootTables;
    private final boolean enableLootUnification;
    private final boolean recipeViewerHiding;
    @Nullable private Set<TagKey<Item>> bakedTagsCache;

    public static Collection<UnifyConfig> safeLoadConfigs() {
        try {
            return loadConfigs();
        } catch (IOException e) {
            AlmostUnified.LOGGER.error("Could not load configs", e);
            return List.of();
        }
    }

    public static Collection<UnifyConfig> loadConfigs() throws IOException {
        Path unifyFolder = Config.createConfigDir().resolve("unify");
        var jsons = readJsons(unifyFolder);
        if (jsons.isEmpty()) {
            Serializer serializer = new Serializer();
            UnifyConfig defaultConfig = serializer.deserialize("materials", new JsonObject());
            Config.save(unifyFolder.resolve("materials.json"), defaultConfig, serializer);
            return List.of(defaultConfig);
        }

        Collection<UnifyConfig> configs = new ArrayList<>();
        for (var entry : jsons.entrySet()) {
            var name = entry.getKey();
            var json = entry.getValue();
            Serializer serializer = new Serializer();
            var config = serializer.deserialize(name, json);
            if (serializer.isInvalid()) {
                AlmostUnified.LOGGER.warn("Unify config not found or invalid. Creating new config: {}", name);
                save(unifyFolder.resolve(config.getName() + ".json"), config, serializer);
            }

            configs.add(config);
        }

        return configs;
    }

    public static Map<String, JsonObject> readJsons(Path directory) {
        Map<String, JsonObject> result = new HashMap<>();
        Gson gson = new Gson();

        try {
            Files.createDirectories(directory);
            var files = FileUtils.listFiles(directory.toFile(), new String[]{ "json" }, false);

            for (var file : files) {
                var fileName = FilenameUtils.getBaseName(file.getName());
                try {
                    var content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                    var jsonObject = gson.fromJson(content, JsonObject.class);
                    result.put(fileName, jsonObject);
                } catch (Throwable e) {
                    AlmostUnified.LOGGER.error("Could not load json from file {}.json: ", fileName, e);
                }
            }
        } catch (Throwable e) {
            AlmostUnified.LOGGER.error("Could not load jsons: ", e);
        }

        return result;
    }

    public UnifyConfig(String name, List<String> modPriorities, Map<TagKey<Item>, String> priorityOverrides, List<String> stoneStrata, List<String> unbakedTags, Set<TagKey<Item>> ignoredTags, Set<Pattern> ignoredItems, Set<Pattern> ignoredRecipeTypes, Set<Pattern> ignoredRecipes, Set<Pattern> ignoredLootTables, boolean enableLootUnification, boolean recipeViewerHiding) {
        super(name);
        this.modPriorities = modPriorities;
        this.priorityOverrides = priorityOverrides;
        this.stoneStrata = stoneStrata;
        this.unbakedTags = unbakedTags;
        this.ignoredTags = ignoredTags;
        this.ignoredItems = ignoredItems;
        this.ignoredRecipeTypes = ignoredRecipeTypes;
        this.ignoredRecipes = ignoredRecipes;
        this.ignoredLootTables = ignoredLootTables;
        this.enableLootUnification = enableLootUnification;
        this.recipeViewerHiding = recipeViewerHiding;
    }

    public ModPriorities getModPriorities() {
        return new ModPrioritiesImpl(modPriorities, priorityOverrides);
    }

    public List<String> getStoneStrata() {
        return stoneStrata;
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

    public boolean hideNonPreferredItemsInRecipeViewers() {
        return recipeViewerHiding;
    }

    public static class Serializer extends Config.Serializer<UnifyConfig> {

        public static final String MOD_PRIORITIES = "modPriorities";
        public static final String STONE_STRATA = "stoneStrata";
        public static final String TAGS = "tags";
        public static final String MATERIALS = "materials";
        public static final String PRIORITY_OVERRIDES = "priorityOverrides";
        public static final String IGNORED_TAGS = "ignoredTags";
        public static final String IGNORED_ITEMS = "ignoredItems";
        public static final String IGNORED_RECIPE_TYPES = "ignoredRecipeTypes";
        public static final String IGNORED_RECIPES = "ignoredRecipes";
        public static final String IGNORED_LOOT_TABLES = "ignoredLootTables";
        public static final String RECIPE_VIEWER_HIDING = "recipeViewerHiding";

        @Override
        public UnifyConfig deserialize(String name, JsonObject json) {
            var platform = AlmostUnifiedPlatform.INSTANCE.getPlatform();

            // Mod priorities
            List<String> modPriorities = safeGet(() -> JsonUtils.toList(json.getAsJsonArray(MOD_PRIORITIES)),
                    Defaults.getModPriorities(platform));

            Map<TagKey<Item>, String> priorityOverrides = safeGet(() -> JsonUtils.deserializeMap(json,
                    PRIORITY_OVERRIDES,
                    e -> TagKey.create(Registries.ITEM, ResourceLocation.parse(e.getKey())),
                    e -> e.getValue().getAsString()), new HashMap<>());

            List<String> stoneStrata = safeGet(() -> JsonUtils.toList(json.getAsJsonArray(STONE_STRATA)),
                    Defaults.STONE_STRATA);
            List<String> tags = safeGet(() -> JsonUtils.toList(json.getAsJsonArray(TAGS)), Defaults.getTags(platform));

            Set<TagKey<Item>> ignoredTags = safeGet(() -> JsonUtils
                    .toList(json.getAsJsonArray(IGNORED_TAGS))
                    .stream()
                    .map(s -> TagKey.create(Registries.ITEM, ResourceLocation.parse(s)))
                    .collect(Collectors.toSet()), new HashSet<>());
            Set<Pattern> ignoredItems = deserializePatterns(json, IGNORED_ITEMS, List.of());
            Set<Pattern> ignoredRecipeTypes = deserializePatterns(json,
                    IGNORED_RECIPE_TYPES,
                    Defaults.getIgnoredRecipeTypes(platform));
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
                    stoneStrata,
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
            json.add(STONE_STRATA, JsonUtils.toArray(config.stoneStrata));
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

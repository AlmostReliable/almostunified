package com.almostreliable.unified.config;

import com.almostreliable.unified.AlmostUnifiedPlatform;
import com.almostreliable.unified.unification.recipe.RecipeLink;
import com.almostreliable.unified.utils.JsonCompare;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class DuplicateConfig extends Config {

    public static final String NAME = "duplicates";
    public static final DuplicateSerializer SERIALIZER = new DuplicateSerializer();

    private final JsonCompare.CompareSettings defaultRules;
    private final LinkedHashMap<ResourceLocation, JsonCompare.CompareSettings> overrideRules;
    private final Set<Pattern> ignoreRecipeTypes;
    private final Set<Pattern> ignoreRecipeIds;
    private final boolean compareAll;

    private final Map<ResourceLocation, Boolean> ignoredRecipeTypesCache;

    private DuplicateConfig(JsonCompare.CompareSettings defaultRules, LinkedHashMap<ResourceLocation, JsonCompare.CompareSettings> overrideRules, Set<Pattern> ignoreRecipeTypes, Set<Pattern> ignoreRecipeIds, boolean compareAll) {
        super(NAME);
        this.defaultRules = defaultRules;
        this.overrideRules = overrideRules;
        this.ignoreRecipeTypes = ignoreRecipeTypes;
        this.ignoreRecipeIds = ignoreRecipeIds;
        this.compareAll = compareAll;
        this.ignoredRecipeTypesCache = new HashMap<>();
    }

    public boolean shouldIgnoreRecipe(RecipeLink recipe) {
        if (isRecipeTypeIgnored(recipe)) {
            return true;
        }

        for (Pattern ignoreRecipePattern : ignoreRecipeIds) {
            if (ignoreRecipePattern.matcher(recipe.getId().toString()).matches()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the recipe type is ignored. This is cached to avoid having to recompute the regex for every recipe.
     *
     * @param recipe The recipe to check
     * @return True if the recipe type is ignored, false otherwise
     */
    private boolean isRecipeTypeIgnored(RecipeLink recipe) {
        return ignoredRecipeTypesCache.computeIfAbsent(recipe.getType(), type -> {
            for (Pattern ignorePattern : ignoreRecipeTypes) {
                if (ignorePattern.matcher(type.toString()).matches()) {
                    return true;
                }
            }
            return false;
        });
    }

    public JsonCompare.CompareSettings getCompareSettings(ResourceLocation type) {
        return overrideRules.getOrDefault(type, defaultRules);
    }

    public boolean shouldCompareAll() {
        return compareAll;
    }

    public void clearCache() {
        ignoredRecipeTypesCache.clear();
    }

    public static final class DuplicateSerializer extends Config.Serializer<DuplicateConfig> {

        private static final String DEFAULT_DUPLICATE_RULES = "default_duplicate_rules";
        private static final String OVERRIDE_DUPLICATE_RULES = "override_duplicate_rules";
        private static final String IGNORED_RECIPE_TYPES = "ignored_recipe_types";
        private static final String IGNORED_RECIPE_IDS = "ignored_recipe_ids";
        private static final String COMPARE_ALL = "compare_all";

        private DuplicateSerializer() {}

        @Override
        public DuplicateConfig handleDeserialization(JsonObject json) {
            var platform = AlmostUnifiedPlatform.INSTANCE.getPlatform();
            Set<Pattern> ignoreRecipeTypes = deserializePatterns(
                json,
                IGNORED_RECIPE_TYPES,
                Defaults.IGNORED_RECIPE_TYPES
            );
            Set<Pattern> ignoreRecipeIds = deserializePatterns(json, IGNORED_RECIPE_IDS, List.of());

            JsonCompare.CompareSettings defaultRules = safeGet(() -> createCompareSet(json.getAsJsonObject(
                    DEFAULT_DUPLICATE_RULES)),
                Defaults.getDefaultDuplicateRules(platform));
            LinkedHashMap<ResourceLocation, JsonCompare.CompareSettings> overrideRules = safeGet(() -> getOverrideRules(
                json), Defaults.getDefaultDuplicateOverrides(platform));
            boolean compareAll = safeGet(() -> json.get(COMPARE_ALL).getAsBoolean(), false);

            return new DuplicateConfig(
                defaultRules,
                overrideRules,
                ignoreRecipeTypes,
                ignoreRecipeIds,
                compareAll
            );
        }

        // Extracted as method because `safeGet` couldn't cast the type... Seems to be an old SDK bug :-)
        // https://bugs.openjdk.org/browse/JDK-8324860
        private LinkedHashMap<ResourceLocation, JsonCompare.CompareSettings> getOverrideRules(JsonObject json) {
            return json
                .getAsJsonObject(OVERRIDE_DUPLICATE_RULES)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> ResourceLocation.parse(entry.getKey()),
                    entry -> createCompareSet(entry.getValue().getAsJsonObject()),
                    (a, b) -> b,
                    LinkedHashMap::new));
        }

        private JsonCompare.CompareSettings createCompareSet(JsonObject rules) {
            JsonCompare.CompareSettings result = new JsonCompare.CompareSettings();
            result.deserialize(rules);
            return result;
        }

        @Override
        public JsonObject serialize(DuplicateConfig config) {
            JsonObject json = new JsonObject();

            serializePatterns(json, IGNORED_RECIPE_TYPES, config.ignoreRecipeTypes);
            serializePatterns(json, IGNORED_RECIPE_IDS, config.ignoreRecipeIds);
            json.add(DEFAULT_DUPLICATE_RULES, config.defaultRules.serialize());
            JsonObject overrides = new JsonObject();
            config.overrideRules.forEach((rl, compareSettings) ->
                overrides.add(rl.toString(), compareSettings.serialize()));
            json.add(OVERRIDE_DUPLICATE_RULES, overrides);
            json.addProperty(COMPARE_ALL, false);

            return json;
        }

    }
}

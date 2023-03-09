package com.almostreliable.unified.config;

import com.almostreliable.unified.AlmostUnifiedPlatform;
import com.almostreliable.unified.recipe.RecipeLink;
import com.almostreliable.unified.utils.JsonCompare;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DuplicationConfig extends Config {
    public static final String NAME = "duplicates";

    private final JsonCompare.CompareSettings defaultRules;
    private final LinkedHashMap<ResourceLocation, JsonCompare.CompareSettings> overrideRules;
    private final Set<Pattern> ignoreRecipeTypes;
    private final Set<Pattern> ignoreRecipes;
    private final boolean strictMode;
    private final HashMap<ResourceLocation, Boolean> recipeTypeIgnoredCache;

    public DuplicationConfig(JsonCompare.CompareSettings defaultRules, LinkedHashMap<ResourceLocation, JsonCompare.CompareSettings> overrideRules, Set<Pattern> ignoreRecipeTypes, Set<Pattern> ignoreRecipes, boolean strictMode) {
        this.defaultRules = defaultRules;
        this.overrideRules = overrideRules;
        this.ignoreRecipeTypes = ignoreRecipeTypes;
        this.ignoreRecipes = ignoreRecipes;
        this.strictMode = strictMode;
        this.recipeTypeIgnoredCache = new HashMap<>();
    }

    public boolean shouldIgnoreRecipe(RecipeLink recipe) {
        /*
         * Avoid needlessly computing a regex match on every recipe by caching whether the type should be ignored or not.
         */
        boolean isTypeIgnored = recipeTypeIgnoredCache.computeIfAbsent(recipe.getType(), type -> {
            return ignoreRecipeTypes.stream().anyMatch(pattern -> pattern.matcher(type.toString()).matches());
        });
        return isTypeIgnored ||
               ignoreRecipes.stream().anyMatch(pattern -> pattern.matcher(recipe.getId().toString()).matches());
    }

    public JsonCompare.CompareSettings getCompareSettings(ResourceLocation type) {
        return overrideRules.getOrDefault(type, defaultRules);
    }

    public boolean isStrictMode() {
        return strictMode;
    }

    public static class Serializer extends Config.Serializer<DuplicationConfig> {
        public static final String DEFAULT_DUPLICATE_RULES = "defaultDuplicateRules";
        public static final String OVERRIDE_DUPLICATE_RULES = "overrideDuplicateRules";
        public static final String IGNORED_RECIPE_TYPES = "ignoredRecipeTypes";
        public static final String IGNORED_RECIPES = "ignoredRecipes";
        public static final String STRICT_MODE = "strictMode";

        @Override
        public DuplicationConfig deserialize(JsonObject json) {
            var platform = AlmostUnifiedPlatform.INSTANCE.getPlatform();
            Set<Pattern> ignoreRecipeTypes = deserializePatterns(json,
                    IGNORED_RECIPE_TYPES,
                    Defaults.getIgnoredRecipeTypes(platform));
            Set<Pattern> ignoreRecipes = deserializePatterns(json, IGNORED_RECIPES, List.of());

            JsonCompare.CompareSettings defaultRules = safeGet(() -> createCompareSet(json.getAsJsonObject(
                            DEFAULT_DUPLICATE_RULES)),
                    Defaults.getDefaultDuplicateRules(platform));
            LinkedHashMap<ResourceLocation, JsonCompare.CompareSettings> overrideRules = safeGet(() -> json
                    .getAsJsonObject(OVERRIDE_DUPLICATE_RULES)
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(entry -> new ResourceLocation(entry.getKey()),
                            entry -> createCompareSet(entry.getValue().getAsJsonObject()),
                            (a, b) -> b,
                            LinkedHashMap::new)), Defaults.getDefaultDuplicateOverrides(platform));
            boolean strictMode = safeGet(() -> json.get(STRICT_MODE).getAsBoolean(), false);

            return new DuplicationConfig(defaultRules, overrideRules, ignoreRecipeTypes, ignoreRecipes, strictMode);
        }

        private JsonCompare.CompareSettings createCompareSet(JsonObject rules) {
            JsonCompare.CompareSettings result = new JsonCompare.CompareSettings();
            result.deserialize(rules);
            return result;
        }

        @Override
        public JsonObject serialize(DuplicationConfig config) {
            JsonObject json = new JsonObject();

            serializePatterns(json, IGNORED_RECIPE_TYPES, config.ignoreRecipeTypes);
            serializePatterns(json, IGNORED_RECIPES, config.ignoreRecipes);
            json.add(DEFAULT_DUPLICATE_RULES, config.defaultRules.serialize());
            JsonObject overrides = new JsonObject();
            config.overrideRules.forEach((rl, compareSettings) -> {
                overrides.add(rl.toString(), compareSettings.serialize());
            });
            json.add(OVERRIDE_DUPLICATE_RULES, overrides);
            json.addProperty(STRICT_MODE, false);

            return json;
        }

    }
}

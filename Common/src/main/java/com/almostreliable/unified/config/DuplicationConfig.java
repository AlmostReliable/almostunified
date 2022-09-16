package com.almostreliable.unified.config;

import com.almostreliable.unified.recipe.RecipeLink;
import com.almostreliable.unified.utils.JsonCompare;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

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

    public DuplicationConfig(JsonCompare.CompareSettings defaultRules, LinkedHashMap<ResourceLocation, JsonCompare.CompareSettings> overrideRules, Set<Pattern> ignoreRecipeTypes, Set<Pattern> ignoreRecipes, boolean strictMode) {
        this.defaultRules = defaultRules;
        this.overrideRules = overrideRules;
        this.ignoreRecipeTypes = ignoreRecipeTypes;
        this.ignoreRecipes = ignoreRecipes;
        this.strictMode = strictMode;
    }

    public boolean shouldIgnoreRecipe(RecipeLink recipe) {
        return ignoreRecipeTypes.stream().anyMatch(pattern -> pattern.matcher(recipe.getType().toString()).matches()) ||
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
            Set<Pattern> ignoreRecipeTypes = deserializePatterns(json,
                    IGNORED_RECIPE_TYPES,
                    Defaults.IGNORED_RECIPE_TYPES);
            Set<Pattern> ignoreRecipes = deserializePatterns(json, IGNORED_RECIPES, List.of());

            JsonCompare.CompareSettings defaultRules = safeGet(() -> createCompareSet(json.getAsJsonObject(
                            DEFAULT_DUPLICATE_RULES)),
                    defaultSettings());
            LinkedHashMap<ResourceLocation, JsonCompare.CompareSettings> overrideRules = safeGet(() -> json
                    .getAsJsonObject(OVERRIDE_DUPLICATE_RULES)
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(entry -> new ResourceLocation(entry.getKey()),
                            entry -> createCompareSet(entry.getValue().getAsJsonObject()),
                            (a, b) -> b,
                            LinkedHashMap::new)), defaultOverrides());
            boolean strictMode = safeGet(() -> json.get(STRICT_MODE).getAsBoolean(), false);

            return new DuplicationConfig(defaultRules, overrideRules, ignoreRecipeTypes, ignoreRecipes, strictMode);
        }

        private JsonCompare.CompareSettings defaultSettings() {
            JsonCompare.CompareSettings result = new JsonCompare.CompareSettings();
            result.ignoreField("conditions");
            result.ignoreField("group");
            result.addRule("cookingtime", new JsonCompare.HigherRule());
            result.addRule("energy", new JsonCompare.HigherRule());
            result.addRule("experience", new JsonCompare.HigherRule());
            return result;
        }

        private LinkedHashMap<ResourceLocation, JsonCompare.CompareSettings> defaultOverrides() {
            JsonCompare.CompareSettings result = new JsonCompare.CompareSettings();
            result.ignoreField("conditions");
            result.ignoreField("group");
            result.ignoreField("pattern");
            result.ignoreField("key");
            LinkedHashMap<ResourceLocation, JsonCompare.CompareSettings> resultMap = new LinkedHashMap<>();
            resultMap.put(new ResourceLocation("minecraft", "crafting_shaped"), result);
            return resultMap;
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

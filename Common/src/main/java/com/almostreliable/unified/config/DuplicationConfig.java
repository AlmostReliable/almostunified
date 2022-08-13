package com.almostreliable.unified.config;

import com.almostreliable.unified.BuildConfig;
import com.almostreliable.unified.recipe.RecipeLink;
import com.almostreliable.unified.utils.JsonCompare;
import com.almostreliable.unified.utils.JsonUtils;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.stream.Collectors;

public class DuplicationConfig extends Config {
    public static final String NAME = "duplicates";
    private final JsonCompare.CompareSettings defaultRules;
    private final LinkedHashMap<ResourceLocation, JsonCompare.CompareSettings> overrideRules;
    private final Set<ResourceLocation> ignoreRecipeTypes;
    private final Set<ResourceLocation> ignoreRecipes;

    public DuplicationConfig(JsonCompare.CompareSettings defaultRules, LinkedHashMap<ResourceLocation, JsonCompare.CompareSettings> overrideRules, Set<ResourceLocation> ignoreRecipeTypes, Set<ResourceLocation> ignoreRecipes) {
        this.defaultRules = defaultRules;
        this.overrideRules = overrideRules;
        this.ignoreRecipeTypes = ignoreRecipeTypes;
        this.ignoreRecipes = ignoreRecipes;
    }

    public boolean shouldIgnoreRecipe(RecipeLink recipe) {
        return ignoreRecipeTypes.contains(recipe.getType()) || ignoreRecipes.contains(recipe.getId());
    }

    public JsonCompare.CompareSettings getCompareSettings(ResourceLocation type) {
        return overrideRules.getOrDefault(type, defaultRules);
    }

    public static class Serializer extends Config.Serializer<DuplicationConfig> {
        public static final String DEFAULT_DUPLICATE_RULES = "defaultDuplicateRules";
        public static final String OVERRIDE_DUPLICATE_RULES = "overrideDuplicateRules";
        public static final String IGNORED_RECIPE_TYPES = "ignoredRecipeTypes";
        public static final String IGNORED_RECIPES = "ignoredRecipes";

        @Override
        public DuplicationConfig deserialize(JsonObject json) {
            Set<ResourceLocation> ignoreRecipeTypes = safeGet(() -> JsonUtils
                    .toList(json.getAsJsonArray(IGNORED_RECIPE_TYPES))
                    .stream()
                    .map(ResourceLocation::new)
                    .collect(Collectors.toSet()), new HashSet<>());
            Set<ResourceLocation> ignoreRecipes = safeGet(() -> JsonUtils
                    .toList(json.getAsJsonArray(IGNORED_RECIPES))
                    .stream()
                    .map(ResourceLocation::new)
                    .collect(Collectors.toSet()), new HashSet<>());

            JsonCompare.CompareSettings defaultRules = safeGet(() -> createCompareSet(json.getAsJsonObject(
                            DEFAULT_DUPLICATE_RULES)),
                    defaultSet());
            LinkedHashMap<ResourceLocation, JsonCompare.CompareSettings> overrideRules = safeGet(() -> {
                LinkedHashMap<ResourceLocation, JsonCompare.CompareSettings> overrides = new LinkedHashMap<>();
                json.getAsJsonObject(OVERRIDE_DUPLICATE_RULES).entrySet().forEach(entry -> {
                    overrides.put(new ResourceLocation(entry.getKey()),
                            createCompareSet(entry.getValue().getAsJsonObject()));
                });
                return overrides;
            }, new LinkedHashMap<>());

            return new DuplicationConfig(defaultRules, overrideRules, ignoreRecipeTypes, ignoreRecipes);
        }

        private JsonCompare.CompareSettings defaultSet() {
            JsonCompare.CompareSettings result = new JsonCompare.CompareSettings();
            result.ignoreField("conditions");
            result.addRule("cookingtime", new JsonCompare.HigherRule());
            result.addRule("energy", new JsonCompare.HigherRule());
            result.addRule("experience", new JsonCompare.HigherRule());
            return result;
        }

        private JsonCompare.CompareSettings createCompareSet(JsonObject rules) {
            JsonCompare.CompareSettings result = new JsonCompare.CompareSettings();
            result.deserialize(rules);
            return result;
        }

        @Override
        public JsonObject serialize(DuplicationConfig config) {
            JsonObject json = new JsonObject();

            json.add(IGNORED_RECIPE_TYPES,
                    JsonUtils.toArray(config.ignoreRecipeTypes
                            .stream()
                            .map(ResourceLocation::toString)
                            .collect(Collectors.toList())));
            json.add(IGNORED_RECIPES,
                    JsonUtils.toArray(config.ignoreRecipes
                            .stream()
                            .map(ResourceLocation::toString)
                            .collect(Collectors.toList())));
            json.add(DEFAULT_DUPLICATE_RULES, config.defaultRules.serialize());
            JsonObject overrides = new JsonObject();
            config.overrideRules.forEach((rl, compareSettings) -> {
                overrides.add(rl.toString(), compareSettings.serialize());
            });
            json.add(OVERRIDE_DUPLICATE_RULES, overrides);

            return json;
        }

    }
}

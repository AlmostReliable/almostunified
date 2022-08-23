package com.almostreliable.unified.config;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.AlmostUnifiedPlatform;
import com.almostreliable.unified.utils.JsonUtils;
import com.almostreliable.unified.utils.UnifyTag;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.*;
import java.util.stream.Collectors;

public class UnifyConfig extends Config {
    public static final String NAME = "unify";

    private final List<String> stoneStrata;
    private final List<String> materials;
    private final List<String> unbakedTags;
    private final List<String> modPriorities;
    private final Set<UnifyTag<Item>> ignoredTags;
    private final Set<ResourceLocation> ignoredRecipeTypes;
    private final Set<ResourceLocation> ignoredRecipes;
    private final boolean hideJeiRei;

    public UnifyConfig(List<String> stoneStrata, List<String> materials, List<String> unbakedTags, List<String> modPriorities, Set<UnifyTag<Item>> ignoredTags, Set<ResourceLocation> ignoredRecipeTypes, Set<ResourceLocation> ignoredRecipes, boolean hideJeiRei) {
        this.stoneStrata = stoneStrata;
        this.materials = materials;
        this.unbakedTags = unbakedTags;
        this.modPriorities = modPriorities;
        this.ignoredTags = ignoredTags;
        this.ignoredRecipeTypes = ignoredRecipeTypes;
        this.ignoredRecipes = ignoredRecipes;
        this.hideJeiRei = hideJeiRei;
    }

    public List<String> getStoneStrata() {
        return Collections.unmodifiableList(stoneStrata);
    }

    public List<String> getModPriorities() {
        return Collections.unmodifiableList(modPriorities);
    }

    public boolean includeRecipe(ResourceLocation recipe) {
        return !ignoredRecipes.contains(recipe);
    }

    public boolean includeRecipeType(ResourceLocation type) {
        return !ignoredRecipeTypes.contains(type);
    }

    public List<UnifyTag<Item>> bakeTags() {
        List<UnifyTag<Item>> result = new ArrayList<>();

        for (String tag : unbakedTags) {
            for (String material : materials) {
                String replace = tag.replace("{material}", material);
                ResourceLocation asRL = ResourceLocation.tryParse(replace);
                if (asRL == null) {
                    AlmostUnified.LOG.warn("Could not bake tag <{}> with material <{}>", tag, material);
                } else {
                    UnifyTag<Item> t = UnifyTag.item(asRL);
                    if (!ignoredTags.contains(t)) {
                        result.add(t);
                    }
                }
            }
        }

        return result;
    }

    public boolean reiOrJeiDisabled() {
        return !hideJeiRei;
    }

    public static class Serializer extends Config.Serializer<UnifyConfig> {

        public static final String STONE_STRATA = "stoneStrata";
        public static final String MOD_PRIORITIES = "modPriorities";
        public static final String MATERIALS = "materials";
        public static final String TAGS = "tags";
        public static final String IGNORED_TAGS = "ignoredTags";
        public static final String IGNORED_RECIPE_TYPES = "ignoredRecipeTypes";
        public static final String IGNORED_RECIPES = "ignoredRecipes";

        public static final String HIDE_JEI_REI = "itemsHidingJeiRei";

        @Override
        public UnifyConfig deserialize(JsonObject json) {
            List<String> stoneStrata = safeGet(() -> JsonUtils.toList(json.getAsJsonArray(STONE_STRATA)),
                    Defaults.STONE_STRATA);
            List<String> mods = safeGet(() -> JsonUtils.toList(json.getAsJsonArray(MOD_PRIORITIES)),
                    Defaults.MOD_PRIORITIES);
            List<String> materials = safeGet(() -> JsonUtils.toList(json.getAsJsonArray(MATERIALS)),
                    Defaults.MATERIALS);
            List<String> tags = safeGet(() -> JsonUtils.toList(json.getAsJsonArray(TAGS)),
                    Defaults.getTags(AlmostUnifiedPlatform.INSTANCE.getPlatform()));
            Set<UnifyTag<Item>> ignoredTags = safeGet(() -> JsonUtils
                    .toList(json.getAsJsonArray(IGNORED_TAGS))
                    .stream()
                    .map(s -> UnifyTag.item(new ResourceLocation(s)))
                    .collect(Collectors.toSet()), new HashSet<>());
            Set<ResourceLocation> ignoredRecipeTypes = deserializeResourceLocations(json, IGNORED_RECIPE_TYPES);
            Set<ResourceLocation> ignoredRecipes = deserializeResourceLocations(json, IGNORED_RECIPES);
            boolean hideJeiRei = safeGet(() -> json.getAsJsonPrimitive(HIDE_JEI_REI).getAsBoolean(), true);

            return new UnifyConfig(stoneStrata,
                    materials,
                    tags,
                    mods,
                    ignoredTags,
                    ignoredRecipeTypes,
                    ignoredRecipes,
                    hideJeiRei);
        }

        @Override
        public JsonObject serialize(UnifyConfig config) {
            JsonObject json = new JsonObject();
            json.add(MOD_PRIORITIES, JsonUtils.toArray(config.modPriorities));
            json.add(STONE_STRATA, JsonUtils.toArray(config.stoneStrata));
            json.add(TAGS, JsonUtils.toArray(config.unbakedTags));
            json.add(MATERIALS, JsonUtils.toArray(config.materials));
            json.add(IGNORED_TAGS,
                    JsonUtils.toArray(config.ignoredTags
                            .stream()
                            .map(UnifyTag::location)
                            .map(ResourceLocation::toString)
                            .toList()));
            serializeResourceLocations(json, IGNORED_RECIPE_TYPES, config.ignoredRecipeTypes);
            serializeResourceLocations(json, IGNORED_RECIPES, config.ignoredRecipes);
            json.add(HIDE_JEI_REI, new JsonPrimitive(config.hideJeiRei));
            return json;
        }
    }
}

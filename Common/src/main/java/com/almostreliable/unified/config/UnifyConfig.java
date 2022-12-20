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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UnifyConfig extends Config {
    public static final String NAME = "unify";

    private final List<String> modPriorities;
    private final List<String> stoneStrata;
    private final List<String> unbakedTags;
    private final List<String> materials;
    private final Map<ResourceLocation, String> priorityOverrides;
    private final Set<UnifyTag<Item>> ignoredTags;
    private final Set<Pattern> ignoredItems;
    private final Set<Pattern> ignoredRecipeTypes;
    private final Set<Pattern> ignoredRecipes;
    private final boolean hideJeiRei;

    public UnifyConfig(List<String> modPriorities, List<String> stoneStrata, List<String> unbakedTags, List<String> materials, Map<ResourceLocation, String> priorityOverrides, Set<UnifyTag<Item>> ignoredTags, Set<Pattern> ignoredItems, Set<Pattern> ignoredRecipeTypes, Set<Pattern> ignoredRecipes, boolean hideJeiRei) {
        this.modPriorities = modPriorities;
        this.stoneStrata = stoneStrata;
        this.unbakedTags = unbakedTags;
        this.materials = materials;
        this.priorityOverrides = priorityOverrides;
        this.ignoredTags = ignoredTags;
        this.ignoredItems = ignoredItems;
        this.ignoredRecipeTypes = ignoredRecipeTypes;
        this.ignoredRecipes = ignoredRecipes;
        this.hideJeiRei = hideJeiRei;
    }

    public List<String> getModPriorities() {
        return Collections.unmodifiableList(modPriorities);
    }

    public List<String> getStoneStrata() {
        return Collections.unmodifiableList(stoneStrata);
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

    // exposed for KubeJS binding
    @SuppressWarnings("unused")
    public List<String> getMaterials() {
        return Collections.unmodifiableList(materials);
    }

    public Map<ResourceLocation, String> getPriorityOverrides() {
        return Collections.unmodifiableMap(priorityOverrides);
    }

    public boolean includeItem(ResourceLocation item) {
        return ignoredItems.stream().noneMatch(pattern -> pattern.matcher(item.toString()).matches());
    }

    public boolean includeRecipe(ResourceLocation recipe) {
        return ignoredRecipes.stream().noneMatch(pattern -> pattern.matcher(recipe.toString()).matches());
    }

    public boolean includeRecipeType(ResourceLocation type) {
        return ignoredRecipeTypes.stream().noneMatch(pattern -> pattern.matcher(type.toString()).matches());
    }

    public boolean reiOrJeiDisabled() {
        return !hideJeiRei;
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
        public static final String HIDE_JEI_REI = "itemsHidingJeiRei";

        @Override
        public UnifyConfig deserialize(JsonObject json) {
            var platform = AlmostUnifiedPlatform.INSTANCE.getPlatform();
            List<String> modPriorities = safeGet(() -> JsonUtils.toList(json.getAsJsonArray(MOD_PRIORITIES)),
                    Defaults.getModPriorities(platform));
            List<String> stoneStrata = safeGet(() -> JsonUtils.toList(json.getAsJsonArray(STONE_STRATA)),
                    Defaults.STONE_STRATA);
            List<String> tags = safeGet(() -> JsonUtils.toList(json.getAsJsonArray(TAGS)), Defaults.getTags(platform));
            List<String> materials = safeGet(() -> JsonUtils.toList(json.getAsJsonArray(MATERIALS)),
                    Defaults.MATERIALS);
            Map<ResourceLocation, String> priorityOverrides = safeGet(() -> json.getAsJsonObject(PRIORITY_OVERRIDES)
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            entry -> new ResourceLocation(entry.getKey()),
                            entry -> entry.getValue().getAsString(),
                            (a, b) -> b,
                            HashMap::new)), new HashMap<>());
            Set<UnifyTag<Item>> ignoredTags = safeGet(() -> JsonUtils
                    .toList(json.getAsJsonArray(IGNORED_TAGS))
                    .stream()
                    .map(s -> UnifyTag.item(new ResourceLocation(s)))
                    .collect(Collectors.toSet()), new HashSet<>());
            Set<Pattern> ignoredItems = deserializePatterns(json, IGNORED_ITEMS, List.of());
            Set<Pattern> ignoredRecipeTypes = deserializePatterns(json, IGNORED_RECIPE_TYPES,
                    Defaults.getIgnoredRecipeTypes(platform));
            Set<Pattern> ignoredRecipes = deserializePatterns(json, IGNORED_RECIPES, List.of());
            boolean hideJeiRei = safeGet(() -> json.getAsJsonPrimitive(HIDE_JEI_REI).getAsBoolean(), true);

            return new UnifyConfig(
                    modPriorities,
                    stoneStrata,
                    tags,
                    materials,
                    priorityOverrides,
                    ignoredTags,
                    ignoredItems,
                    ignoredRecipeTypes,
                    ignoredRecipes,
                    hideJeiRei
            );
        }

        @Override
        public JsonObject serialize(UnifyConfig config) {
            JsonObject json = new JsonObject();
            json.add(MOD_PRIORITIES, JsonUtils.toArray(config.modPriorities));
            json.add(STONE_STRATA, JsonUtils.toArray(config.stoneStrata));
            json.add(TAGS, JsonUtils.toArray(config.unbakedTags));
            json.add(MATERIALS, JsonUtils.toArray(config.materials));
            JsonObject priorityOverrides = new JsonObject();
            config.priorityOverrides.forEach((tag, mod) -> {
                priorityOverrides.add(tag.toString(), new JsonPrimitive(mod));
            });
            json.add(PRIORITY_OVERRIDES, priorityOverrides);
            json.add(IGNORED_TAGS,
                    JsonUtils.toArray(config.ignoredTags
                            .stream()
                            .map(UnifyTag::location)
                            .map(ResourceLocation::toString)
                            .toList()));
            serializePatterns(json, IGNORED_ITEMS, config.ignoredItems);
            serializePatterns(json, IGNORED_RECIPE_TYPES, config.ignoredRecipeTypes);
            serializePatterns(json, IGNORED_RECIPES, config.ignoredRecipes);
            json.add(HIDE_JEI_REI, new JsonPrimitive(config.hideJeiRei));
            return json;
        }
    }
}

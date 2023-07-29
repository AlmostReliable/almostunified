package com.almostreliable.unified.config;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.AlmostUnifiedPlatform;
import com.almostreliable.unified.utils.JsonUtils;
import com.almostreliable.unified.utils.UnifyTag;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
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
    private final Map<ResourceLocation, Set<ResourceLocation>> tagOwnerships;
    private final Set<UnifyTag<Item>> ignoredTags;
    private final Set<Pattern> ignoredItems;
    private final Set<Pattern> ignoredRecipeTypes;
    private final Set<Pattern> ignoredRecipes;
    private final boolean hideJeiRei;

    private final Map<ResourceLocation, Boolean> ignoredRecipeTypesCache;
    @Nullable private Set<UnifyTag<Item>> bakedTagsCache;

    public UnifyConfig(
            List<String> modPriorities,
            List<String> stoneStrata,
            List<String> unbakedTags,
            List<String> materials,
            Map<ResourceLocation, String> priorityOverrides,
            Map<ResourceLocation, Set<ResourceLocation>> tagOwnerships,
//            boolean tagInheritance,
//            Set<Pattern> ignoredInheritance,
            Set<UnifyTag<Item>> ignoredTags,
            Set<Pattern> ignoredItems,
            Set<Pattern> ignoredRecipeTypes,
            Set<Pattern> ignoredRecipes,
            boolean hideJeiRei
    ) {
        this.modPriorities = modPriorities;
        this.stoneStrata = stoneStrata;
        this.unbakedTags = unbakedTags;
        this.materials = materials;
        this.priorityOverrides = priorityOverrides;
        this.tagOwnerships = tagOwnerships;
        this.ignoredTags = ignoredTags;
        this.ignoredItems = ignoredItems;
        this.ignoredRecipeTypes = ignoredRecipeTypes;
        this.ignoredRecipes = ignoredRecipes;
        this.hideJeiRei = hideJeiRei;
        this.ignoredRecipeTypesCache = new HashMap<>();
    }

    public List<String> getModPriorities() {
        return Collections.unmodifiableList(modPriorities);
    }

    public List<String> getStoneStrata() {
        return Collections.unmodifiableList(stoneStrata);
    }

    public Set<UnifyTag<Item>> bakeTags() {
        if (bakedTagsCache != null) {
            return bakedTagsCache;
        }

        Set<UnifyTag<Item>> result = new HashSet<>();

        for (String tag : unbakedTags) {
            for (String material : materials) {
                String replace = tag.replace("{material}", material);
                ResourceLocation asRL = ResourceLocation.tryParse(replace);
                if (asRL == null) {
                    AlmostUnified.LOG.warn("Could not bake tag <{}> with material <{}>", tag, material);
                    continue;
                }

                UnifyTag<Item> t = UnifyTag.item(asRL);
                if (!ignoredTags.contains(t)) {
                    result.add(t);
                }
            }
        }

        bakedTagsCache = result;
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

    public Map<ResourceLocation, Set<ResourceLocation>> getTagOwnerships() {
        return Collections.unmodifiableMap(tagOwnerships);
    }

    public boolean includeItem(ResourceLocation item) {
        for (Pattern pattern : ignoredItems) {
            if (pattern.matcher(item.toString()).matches()) {
                return false;
            }
        }
        return true;
    }

    public boolean includeRecipe(ResourceLocation recipe) {
        for (Pattern pattern : ignoredRecipes) {
            if (pattern.matcher(recipe.toString()).matches()) {
                return false;
            }
        }
        return true;
    }

    public boolean includeRecipeType(ResourceLocation type) {
        return ignoredRecipeTypesCache.computeIfAbsent(type, t -> {
            for (Pattern pattern : ignoredRecipeTypes) {
                if (pattern.matcher(t.toString()).matches()) {
                    return false;
                }
            }
            return true;
        });
    }

    public boolean reiOrJeiDisabled() {
        return !hideJeiRei;
    }

    public void clearCache() {
        ignoredRecipeTypesCache.clear();
    }

    public static class Serializer extends Config.Serializer<UnifyConfig> {

        public static final String MOD_PRIORITIES = "modPriorities";
        public static final String STONE_STRATA = "stoneStrata";
        public static final String TAGS = "tags";
        public static final String MATERIALS = "materials";
        public static final String PRIORITY_OVERRIDES = "priorityOverrides";
        public static final String TAG_OWNERSHIPS = "tagOwnerships";
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
            Map<ResourceLocation, Set<ResourceLocation>> tagOwnerships = safeGet(() -> json
                    .getAsJsonObject(TAG_OWNERSHIPS)
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            entry -> new ResourceLocation(entry.getKey()),
                            entry -> JsonUtils.toList(entry.getValue().getAsJsonArray())
                                    .stream()
                                    .map(ResourceLocation::new)
                                    .collect(Collectors.toSet()),
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
                    tagOwnerships,
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
                priorityOverrides.addProperty(tag.toString(), mod);
            });
            json.add(PRIORITY_OVERRIDES, priorityOverrides);
            JsonObject tagOwnerships = new JsonObject();
            config.tagOwnerships.forEach((parent, child) -> {
                tagOwnerships.add(parent.toString(),
                        JsonUtils.toArray(child.stream().map(ResourceLocation::toString).toList()));
            });
            json.add(TAG_OWNERSHIPS, tagOwnerships);
            json.add(IGNORED_TAGS,
                    JsonUtils.toArray(config.ignoredTags
                            .stream()
                            .map(UnifyTag::location)
                            .map(ResourceLocation::toString)
                            .toList()));
            serializePatterns(json, IGNORED_ITEMS, config.ignoredItems);
            serializePatterns(json, IGNORED_RECIPE_TYPES, config.ignoredRecipeTypes);
            serializePatterns(json, IGNORED_RECIPES, config.ignoredRecipes);
            json.addProperty(HIDE_JEI_REI, config.hideJeiRei);
            return json;
        }
    }
}

package com.almostreliable.unified.config;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.AlmostUnifiedPlatform;
import com.almostreliable.unified.utils.JsonUtils;
import com.almostreliable.unified.utils.UnifyTag;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
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
    private final Enum<TagInheritanceMode> itemTagInheritanceMode;
    private final Map<ResourceLocation, Set<Pattern>> itemTagInheritance;
    private final Enum<TagInheritanceMode> blockTagInheritanceMode;
    private final Map<ResourceLocation, Set<Pattern>> blockTagInheritance;
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
            Enum<TagInheritanceMode> itemTagInheritanceMode,
            Map<ResourceLocation, Set<Pattern>> itemTagInheritance,
            Enum<TagInheritanceMode> blockTagInheritanceMode,
            Map<ResourceLocation, Set<Pattern>> blockTagInheritance,
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
        this.itemTagInheritanceMode = itemTagInheritanceMode;
        this.itemTagInheritance = itemTagInheritance;
        this.blockTagInheritanceMode = blockTagInheritanceMode;
        this.blockTagInheritance = blockTagInheritance;
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
        return bakeTags($ -> true);
    }

    public Set<UnifyTag<Item>> bakeAndValidateTags(Map<ResourceLocation, Collection<Holder<Item>>> tags) {
        return bakeTags(tags::containsKey);
    }

    private Set<UnifyTag<Item>> bakeTags(Predicate<ResourceLocation> tagValidator) {
        if (bakedTagsCache != null) {
            return bakedTagsCache;
        }

        Set<UnifyTag<Item>> result = new HashSet<>();
        Set<UnifyTag<Item>> wrongTags = new HashSet<>();

        for (String tag : unbakedTags) {
            for (String material : materials) {
                String replace = tag.replace("{material}", material);
                ResourceLocation asRL = ResourceLocation.tryParse(replace);
                if (asRL == null) {
                    AlmostUnified.LOG.warn("Could not bake tag <{}> with material <{}>", tag, material);
                    continue;
                }

                UnifyTag<Item> t = UnifyTag.item(asRL);
                if (ignoredTags.contains(t)) continue;

                if (!tagValidator.test(asRL)) {
                    wrongTags.add(t);
                    continue;
                }

                result.add(t);
            }
        }

        if (!wrongTags.isEmpty()) {
            AlmostUnified.LOG.warn(
                    "The following tags are invalid and will be ignored: {}",
                    wrongTags.stream().map(UnifyTag::location).collect(Collectors.toList())
            );
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

    public boolean shouldInheritItemTag(UnifyTag<Item> itemTag, Set<UnifyTag<Item>> dominantTags) {
        var patterns = itemTagInheritance.get(itemTag.location());
        boolean result = checkPatterns(dominantTags, patterns);
        // noinspection SimplifiableConditionalExpression
        return itemTagInheritanceMode == TagInheritanceMode.ALLOW ? result : !result;
    }

    public boolean shouldInheritBlockTag(UnifyTag<Block> itemTag, Set<UnifyTag<Item>> dominantTags) {
        var patterns = blockTagInheritance.get(itemTag.location());
        boolean result = checkPatterns(dominantTags, patterns);
        // noinspection SimplifiableConditionalExpression
        return blockTagInheritanceMode == TagInheritanceMode.ALLOW ? result : !result;
    }

    /**
     * Checks all patterns against all dominant tags.
     * <p>
     * This implementation works based on the assumption that the mode is {@link TagInheritanceMode#ALLOW}.
     * Flip the result if the mode is {@link TagInheritanceMode#DENY}.
     *
     * @param dominantTags The tags of the dominant item to check.
     * @param patterns     The patterns to check against.
     * @param <T>          The type of the dominant tags.
     * @return Whether the dominant tags match any of the patterns.
     */
    private static <T> boolean checkPatterns(Set<UnifyTag<T>> dominantTags, @Nullable Set<Pattern> patterns) {
        if (patterns == null) return false;

        for (var pattern : patterns) {
            for (var dominantTag : dominantTags) {
                if (pattern.matcher(dominantTag.location().toString()).matches()) {
                    return true;
                }
            }
        }

        return false;
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
        public static final String ITEM_TAG_INHERITANCE_MODE = "itemTagInheritanceMode";
        public static final String ITEM_TAG_INHERITANCE = "itemTagInheritance";
        public static final String BLOCK_TAG_INHERITANCE_MODE = "blockTagInheritanceMode";
        public static final String BLOCK_TAG_INHERITANCE = "blockTagInheritance";
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


            Enum<TagInheritanceMode> itemTagInheritanceMode = deserializeTagInheritanceMode(json,
                    ITEM_TAG_INHERITANCE_MODE);
            Map<ResourceLocation, Set<Pattern>> itemTagInheritance = deserializePatternsForLocations(json,
                    ITEM_TAG_INHERITANCE);
            Enum<TagInheritanceMode> blockTagInheritanceMode = deserializeTagInheritanceMode(json,
                    BLOCK_TAG_INHERITANCE_MODE);
            Map<ResourceLocation, Set<Pattern>> blockTagInheritance = deserializePatternsForLocations(json,
                    BLOCK_TAG_INHERITANCE);

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
                    itemTagInheritanceMode,
                    itemTagInheritance,
                    blockTagInheritanceMode,
                    blockTagInheritance,
                    ignoredTags,
                    ignoredItems,
                    ignoredRecipeTypes,
                    ignoredRecipes,
                    hideJeiRei
            );
        }

        private TagInheritanceMode deserializeTagInheritanceMode(JsonObject json, String key) {
            return safeGet(() -> TagInheritanceMode.valueOf(json
                    .getAsJsonPrimitive(key)
                    .getAsString().toUpperCase()), TagInheritanceMode.ALLOW);
        }

        /**
         * Deserialize a list of patterns from a json object with a base key. Example json:
         * <pre>
         *     {
         *          "baseKey": {
         *              "location1": [ pattern1, pattern2 ],
         *              "location2": [ pattern3, pattern4 ]
         *          }
         *     }
         * </pre>
         *
         * @param rawConfigJson The raw config json
         * @param baseKey       The base key
         * @return The deserialized patterns separated by location
         */
        private Map<ResourceLocation, Set<Pattern>> unsafeDeserializePatternsForLocations(JsonObject rawConfigJson, String baseKey) {
            JsonObject json = rawConfigJson.getAsJsonObject(baseKey);
            return json
                    .keySet()
                    .stream()
                    .collect(Collectors.toMap(
                            ResourceLocation::new,
                            key -> deserializePatterns(json, key, List.of()),
                            (a, b) -> b,
                            HashMap::new));
        }

        private Map<ResourceLocation, Set<Pattern>> deserializePatternsForLocations(JsonObject rawConfigJson, String baseKey) {
            return safeGet(() -> unsafeDeserializePatternsForLocations(rawConfigJson, baseKey), new HashMap<>());
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
            JsonObject itemTagInheritance = new JsonObject();
            config.itemTagInheritance.forEach((tag, patterns) -> {
                itemTagInheritance.add(tag.toString(),
                        JsonUtils.toArray(patterns.stream().map(Pattern::toString).toList()));
            });
            json.add(ITEM_TAG_INHERITANCE_MODE, new JsonPrimitive(config.itemTagInheritanceMode.toString()));
            json.add(ITEM_TAG_INHERITANCE, itemTagInheritance);
            JsonObject blockTagInheritance = new JsonObject();
            config.blockTagInheritance.forEach((tag, patterns) -> {
                blockTagInheritance.add(tag.toString(),
                        JsonUtils.toArray(patterns.stream().map(Pattern::toString).toList()));
            });
            json.add(BLOCK_TAG_INHERITANCE_MODE, new JsonPrimitive(config.blockTagInheritanceMode.toString()));
            json.add(BLOCK_TAG_INHERITANCE, blockTagInheritance);
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

    public enum TagInheritanceMode {
        ALLOW,
        DENY
    }
}

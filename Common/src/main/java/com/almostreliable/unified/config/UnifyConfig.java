package com.almostreliable.unified.config;

import com.almostreliable.unified.AlmostUnifiedPlatform;
import com.almostreliable.unified.api.ModPriorities;
import com.almostreliable.unified.api.Replacements;
import com.almostreliable.unified.recipe.ModPrioritiesImpl;
import com.almostreliable.unified.utils.JsonUtils;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("ReplaceNullCheck")
public class UnifyConfig extends Config {
    public static final String NAME = "unify";

    private final List<String> modPriorities;
    private final Map<TagKey<Item>, String> priorityOverrides;
    private final List<String> stoneStrata;
    private final List<String> unbakedTags;
    private final Set<TagKey<Item>> ignoredTags;
    private final Set<Pattern> ignoredItems;
    private final Set<Pattern> ignoredRecipeTypes;
    private final Set<Pattern> ignoredRecipes;
    private final boolean recipeViewerHiding;

    private final Map<ResourceLocation, Boolean> ignoredRecipeTypesCache;
    @Nullable private Set<TagKey<Item>> bakedTagsCache;

    public UnifyConfig(List<String> modPriorities, Map<TagKey<Item>, String> priorityOverrides, List<String> stoneStrata, List<String> unbakedTags, Set<TagKey<Item>> ignoredTags, Set<Pattern> ignoredItems, Set<Pattern> ignoredRecipeTypes, Set<Pattern> ignoredRecipes, boolean recipeViewerHiding) {
        this.modPriorities = modPriorities;
        this.priorityOverrides = priorityOverrides;
        this.stoneStrata = stoneStrata;
        this.unbakedTags = unbakedTags;
        this.ignoredTags = ignoredTags;
        this.ignoredItems = ignoredItems;
        this.ignoredRecipeTypes = ignoredRecipeTypes;
        this.ignoredRecipes = ignoredRecipes;
        this.recipeViewerHiding = recipeViewerHiding;
        this.ignoredRecipeTypesCache = new HashMap<>();
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

    public Set<TagKey<Item>> bakeTags(Predicate<TagKey<Item>> tagValidator, Replacements replacements) {
        if (bakedTagsCache != null) {
            return bakedTagsCache;
        }

        Set<TagKey<Item>> result = new HashSet<>();
        for (var unbakedTag : unbakedTags) {
            var inflate = replacements.inflate(unbakedTag);
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

    public boolean hideNonPreferredItemsInRecipeViewers() {
        return recipeViewerHiding;
    }

    public String getName() {
        return NAME;
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
        public static final String RECIPE_VIEWER_HIDING = "recipeViewerHiding";

        @Override
        public UnifyConfig deserialize(JsonObject json) {
            var platform = AlmostUnifiedPlatform.INSTANCE.getPlatform();

            // Mod priorities
            List<String> modPriorities = safeGet(() -> JsonUtils.toList(json.getAsJsonArray(MOD_PRIORITIES)),
                    Defaults.getModPriorities(platform));

            Map<TagKey<Item>, String> priorityOverrides = safeGet(() -> JsonUtils.deserializeMap(json,
                    PRIORITY_OVERRIDES,
                    e -> TagKey.create(Registries.ITEM, new ResourceLocation(e.getKey())),
                    e -> e.getValue().getAsString()), new HashMap<>());

            List<String> stoneStrata = safeGet(() -> JsonUtils.toList(json.getAsJsonArray(STONE_STRATA)),
                    Defaults.STONE_STRATA);
            List<String> tags = safeGet(() -> JsonUtils.toList(json.getAsJsonArray(TAGS)), Defaults.getTags(platform));
            List<String> materials = safeGet(() -> JsonUtils.toList(json.getAsJsonArray(MATERIALS)),
                    Defaults.MATERIALS);

            Set<TagKey<Item>> ignoredTags = safeGet(() -> JsonUtils
                    .toList(json.getAsJsonArray(IGNORED_TAGS))
                    .stream()
                    .map(s -> TagKey.create(Registries.ITEM, new ResourceLocation(s)))
                    .collect(Collectors.toSet()), new HashSet<>());
            Set<Pattern> ignoredItems = deserializePatterns(json, IGNORED_ITEMS, List.of());
            Set<Pattern> ignoredRecipeTypes = deserializePatterns(json,
                    IGNORED_RECIPE_TYPES,
                    Defaults.getIgnoredRecipeTypes(platform));
            Set<Pattern> ignoredRecipes = deserializePatterns(json, IGNORED_RECIPES, List.of());
            boolean recipeViewerHiding = safeGet(() -> json.getAsJsonPrimitive(RECIPE_VIEWER_HIDING).getAsBoolean(),
                    true);

            return new UnifyConfig(modPriorities,
                    priorityOverrides,
                    stoneStrata,
                    tags,
                    ignoredTags,
                    ignoredItems,
                    ignoredRecipeTypes,
                    ignoredRecipes,
                    recipeViewerHiding);
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
            json.addProperty(RECIPE_VIEWER_HIDING, config.recipeViewerHiding);
            return json;
        }
    }
}

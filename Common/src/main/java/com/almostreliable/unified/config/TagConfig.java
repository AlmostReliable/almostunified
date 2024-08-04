package com.almostreliable.unified.config;

import com.almostreliable.unified.AlmostUnifiedPlatform;
import com.almostreliable.unified.api.ModConstants;
import com.almostreliable.unified.impl.TagInheritance;
import com.almostreliable.unified.utils.JsonUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class TagConfig extends Config {

    public static final String NAME = "tags";
    public static final TagSerializer SERIALIZER = new TagSerializer();

    private final Map<ResourceLocation, Set<ResourceLocation>> customTags;
    private final Map<ResourceLocation, Set<ResourceLocation>> tagSubstitutions;
    private final TagInheritance.Mode itemTagInheritanceMode;
    private final Map<TagKey<Item>, Set<Pattern>> itemTagInheritance;
    private final TagInheritance.Mode blockTagInheritanceMode;
    private final Map<TagKey<Block>, Set<Pattern>> blockTagInheritance;
    private final boolean emiStrictHiding;

    private TagConfig(Map<ResourceLocation, Set<ResourceLocation>> customTags, Map<ResourceLocation, Set<ResourceLocation>> tagSubstitutions, TagInheritance.Mode itemTagInheritanceMode, Map<TagKey<Item>, Set<Pattern>> itemTagInheritance, TagInheritance.Mode blockTagInheritanceMode, Map<TagKey<Block>, Set<Pattern>> blockTagInheritance, boolean emiStrictHiding) {
        super(NAME);
        this.customTags = customTags;
        this.tagSubstitutions = tagSubstitutions;
        this.itemTagInheritanceMode = itemTagInheritanceMode;
        this.itemTagInheritance = itemTagInheritance;
        this.blockTagInheritanceMode = blockTagInheritanceMode;
        this.blockTagInheritance = blockTagInheritance;
        this.emiStrictHiding = emiStrictHiding;
    }

    public TagInheritance getTagInheritance() {
        return new TagInheritance(itemTagInheritanceMode,
                itemTagInheritance,
                blockTagInheritanceMode,
                blockTagInheritance);
    }

    public Map<ResourceLocation, Set<ResourceLocation>> getCustomTags() {
        return Collections.unmodifiableMap(customTags);
    }

    public Map<ResourceLocation, Set<ResourceLocation>> getTagSubstitutions() {
        return Collections.unmodifiableMap(tagSubstitutions);
    }

    public boolean isEmiHidingStrict() {
        return emiStrictHiding;
    }

    public static final class TagSerializer extends Config.Serializer<TagConfig> {

        private static final String CUSTOM_TAGS = "custom_tags";
        private static final String TAG_SUBSTITUTIONS = "tag_substitutions";
        private static final String ITEM_TAG_INHERITANCE_MODE = "item_tag_inheritance_mode";
        private static final String ITEM_TAG_INHERITANCE = "item_tag_inheritance";
        private static final String BLOCK_TAG_INHERITANCE_MODE = "block_tag_inheritance_mode";
        private static final String BLOCK_TAG_INHERITANCE = "block_tag_inheritance";
        private static final String EMI_STRICT_HIDING = "emi_strict_hiding";

        private TagSerializer() {}

        @Override
        public TagConfig handleDeserialization(JsonObject json) {
            Map<ResourceLocation, Set<ResourceLocation>> customTags = safeGet(() -> JsonUtils.deserializeMapSet(json,
                    CUSTOM_TAGS,
                    e -> ResourceLocation.parse(e.getKey()),
                    ResourceLocation::parse), new HashMap<>());

            Map<ResourceLocation, Set<ResourceLocation>> tagSubstitutions = safeGet(() -> JsonUtils.deserializeMapSet(
                    json,
                    TAG_SUBSTITUTIONS,
                    e -> ResourceLocation.parse(e.getKey()),
                    ResourceLocation::parse), new HashMap<>());

            TagInheritance.Mode itemTagInheritanceMode = deserializeTagInheritanceMode(json,
                    ITEM_TAG_INHERITANCE_MODE);
            Map<TagKey<Item>, Set<Pattern>> itemTagInheritance = deserializePatternsForLocations(Registries.ITEM,
                    json,
                    ITEM_TAG_INHERITANCE);
            TagInheritance.Mode blockTagInheritanceMode = deserializeTagInheritanceMode(json,
                    BLOCK_TAG_INHERITANCE_MODE);
            Map<TagKey<Block>, Set<Pattern>> blockTagInheritance = deserializePatternsForLocations(Registries.BLOCK,
                    json,
                    BLOCK_TAG_INHERITANCE);

            boolean emiStrictHiding = AlmostUnifiedPlatform.INSTANCE.isModLoaded(ModConstants.EMI) ?
                                      safeGet(() -> json.get(EMI_STRICT_HIDING).getAsBoolean(), true) :
                                      false;

            return new TagConfig(
                    customTags,
                    tagSubstitutions,
                    itemTagInheritanceMode,
                    itemTagInheritance,
                    blockTagInheritanceMode,
                    blockTagInheritance,
                    emiStrictHiding
            );
        }

        @Override
        public JsonObject serialize(TagConfig config) {
            JsonObject json = new JsonObject();

            JsonObject customTags = new JsonObject();
            config.customTags.forEach((parent, child) -> {
                customTags.add(parent.toString(),
                        JsonUtils.toArray(child.stream().map(ResourceLocation::toString).toList()));
            });
            json.add(CUSTOM_TAGS, customTags);

            JsonObject tagSubstitutions = new JsonObject();
            config.tagSubstitutions.forEach((parent, child) -> {
                tagSubstitutions.add(parent.toString(),
                        JsonUtils.toArray(child.stream().map(ResourceLocation::toString).toList()));
            });
            json.add(TAG_SUBSTITUTIONS, tagSubstitutions);

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

            if (AlmostUnifiedPlatform.INSTANCE.isModLoaded(ModConstants.EMI)) {
                json.addProperty(EMI_STRICT_HIDING, config.emiStrictHiding);
            }

            return json;
        }

        /**
         * Deserializes a list of patterns from a json object with a base key. Example json:
         * <pre>
         * {
         *   "baseKey": {
         *     "location1": [ pattern1, pattern2 ],
         *     "location2": [ pattern3, pattern4 ]
         *   }
         * }
         * </pre>
         *
         * @param rawConfigJson The raw config json
         * @param baseKey       The base key
         * @return The deserialized patterns separated by location
         */
        private <T> Map<TagKey<T>, Set<Pattern>> unsafeDeserializePatternsForLocations(ResourceKey<Registry<T>> registry, JsonObject rawConfigJson, String baseKey) {
            return JsonUtils.deserializeMapSet(rawConfigJson,
                    baseKey,
                    e -> TagKey.create(registry, ResourceLocation.parse(e.getKey())),
                    Pattern::compile);
        }

        private <T> Map<TagKey<T>, Set<Pattern>> deserializePatternsForLocations(ResourceKey<Registry<T>> registry, JsonObject rawConfigJson, String baseKey) {
            return safeGet(() -> unsafeDeserializePatternsForLocations(registry, rawConfigJson, baseKey),
                    new HashMap<>());
        }


        private TagInheritance.Mode deserializeTagInheritanceMode(JsonObject json, String key) {
            return safeGet(() -> TagInheritance.Mode.valueOf(json
                    .getAsJsonPrimitive(key)
                    .getAsString()
                    .toUpperCase()), TagInheritance.Mode.ALLOW);
        }
    }
}

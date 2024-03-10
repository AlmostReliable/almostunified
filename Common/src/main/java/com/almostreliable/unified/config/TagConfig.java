package com.almostreliable.unified.config;

import com.almostreliable.unified.api.TagInheritance;
import com.almostreliable.unified.impl.TagInheritanceImpl;
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

public class TagConfig extends Config {

    public static final String NAME = "tags";
    private final Map<ResourceLocation, Set<ResourceLocation>> customTags;
    private final Map<ResourceLocation, Set<ResourceLocation>> tagOwnerships;
    private final TagInheritanceImpl.Mode itemTagInheritanceMode;
    private final Map<TagKey<Item>, Set<Pattern>> itemTagInheritance;
    private final TagInheritanceImpl.Mode blockTagInheritanceMode;
    private final Map<TagKey<Block>, Set<Pattern>> blockTagInheritance;

    public TagConfig(String name, Map<ResourceLocation, Set<ResourceLocation>> customTags, Map<ResourceLocation, Set<ResourceLocation>> tagOwnerships, TagInheritanceImpl.Mode itemTagInheritanceMode, Map<TagKey<Item>, Set<Pattern>> itemTagInheritance, TagInheritanceImpl.Mode blockTagInheritanceMode, Map<TagKey<Block>, Set<Pattern>> blockTagInheritance) {
        super(name);
        this.customTags = customTags;
        this.tagOwnerships = tagOwnerships;
        this.itemTagInheritanceMode = itemTagInheritanceMode;
        this.itemTagInheritance = itemTagInheritance;
        this.blockTagInheritanceMode = blockTagInheritanceMode;
        this.blockTagInheritance = blockTagInheritance;
    }

    public TagInheritance<Item> getItemTagInheritance() {
        return new TagInheritanceImpl<>(itemTagInheritanceMode, itemTagInheritance);
    }

    public TagInheritance<Block> getBlockTagInheritance() {
        return new TagInheritanceImpl<>(blockTagInheritanceMode, blockTagInheritance);
    }

    public Map<ResourceLocation, Set<ResourceLocation>> getCustomTags() {
        return Collections.unmodifiableMap(customTags);
    }

    public Map<ResourceLocation, Set<ResourceLocation>> getTagOwnerships() {
        return Collections.unmodifiableMap(tagOwnerships);
    }

    public static class Serializer extends Config.Serializer<TagConfig> {
        public static final String CUSTOM_TAGS = "customTags";
        public static final String TAG_OWNERSHIPS = "tagOwnerships";
        public static final String ITEM_TAG_INHERITANCE_MODE = "itemTagInheritanceMode";
        public static final String ITEM_TAG_INHERITANCE = "itemTagInheritance";
        public static final String BLOCK_TAG_INHERITANCE_MODE = "blockTagInheritanceMode";
        public static final String BLOCK_TAG_INHERITANCE = "blockTagInheritance";

        @Override
        public TagConfig deserialize(String name, JsonObject json) {
            Map<ResourceLocation, Set<ResourceLocation>> customTags = safeGet(() -> JsonUtils.deserializeMapSet(json,
                    CUSTOM_TAGS,
                    e -> new ResourceLocation(e.getKey()),
                    ResourceLocation::new), new HashMap<>());

            Map<ResourceLocation, Set<ResourceLocation>> tagOwnerships = safeGet(() -> JsonUtils.deserializeMapSet(json,
                    TAG_OWNERSHIPS,
                    e -> new ResourceLocation(e.getKey()),
                    ResourceLocation::new), new HashMap<>());

            TagInheritanceImpl.Mode itemTagInheritanceMode = deserializeTagInheritanceMode(json,
                    ITEM_TAG_INHERITANCE_MODE);
            Map<TagKey<Item>, Set<Pattern>> itemTagInheritance = deserializePatternsForLocations(Registries.ITEM,
                    json,
                    ITEM_TAG_INHERITANCE);
            TagInheritanceImpl.Mode blockTagInheritanceMode = deserializeTagInheritanceMode(json,
                    BLOCK_TAG_INHERITANCE_MODE);
            Map<TagKey<Block>, Set<Pattern>> blockTagInheritance = deserializePatternsForLocations(Registries.BLOCK,
                    json,
                    BLOCK_TAG_INHERITANCE);

            return new TagConfig(
                    name,
                    customTags,
                    tagOwnerships,
                    itemTagInheritanceMode,
                    itemTagInheritance,
                    blockTagInheritanceMode,
                    blockTagInheritance
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
                    e -> TagKey.create(registry, new ResourceLocation(e.getKey())),
                    Pattern::compile);
        }

        private <T> Map<TagKey<T>, Set<Pattern>> deserializePatternsForLocations(ResourceKey<Registry<T>> registry, JsonObject rawConfigJson, String baseKey) {
            return safeGet(() -> unsafeDeserializePatternsForLocations(registry, rawConfigJson, baseKey),
                    new HashMap<>());
        }


        private TagInheritanceImpl.Mode deserializeTagInheritanceMode(JsonObject json, String key) {
            return safeGet(() -> TagInheritanceImpl.Mode.valueOf(json
                    .getAsJsonPrimitive(key)
                    .getAsString()
                    .toUpperCase()), TagInheritanceImpl.Mode.ALLOW);
        }
    }
}

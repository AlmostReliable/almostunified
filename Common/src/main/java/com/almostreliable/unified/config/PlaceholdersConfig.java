package com.almostreliable.unified.config;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.api.Placeholders;
import com.almostreliable.unified.utils.JsonUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.function.BiConsumer;

public class PlaceholdersConfig extends Config implements Placeholders {

    public static final String NAME = "placeholders";
    private final Map<String, Collection<String>> replacements;

    public PlaceholdersConfig(String name, Map<String, Collection<String>> replacements) {
        super(name);
        this.replacements = replacements;
    }

    private Collection<String> inflate(Collection<String> input, String key, Collection<String> replacements) {
        Set<String> result = new HashSet<>();
        for (String value : input) {
            for (String replacement : replacements) {
                result.add(value.replace(key, replacement));
            }
        }

        return result;
    }

    public Collection<ResourceLocation> inflate(String str) {
        Collection<String> inputs = new HashSet<>();
        inputs.add(str);
        for (var entry : replacements.entrySet()) {
            inputs = inflate(inputs, "{" + entry.getKey() + "}", entry.getValue());
        }

        Set<ResourceLocation> result = new HashSet<>();
        Set<String> invalid = new HashSet<>();
        for (String input : inputs) {
            var rl = ResourceLocation.tryParse(input);
            if (rl == null) {
                invalid.add(input);
                continue;
            }

            result.add(rl);
        }

        if (!invalid.isEmpty()) {
            AlmostUnified.LOGGER.warn(
                    "The following input '{}' could not be parsed into a ResourceLocation. Reason could be missing placeholder or invalid characters. Skipping. Generated values: {}",
                    str,
                    invalid);
        }

        return result;
    }

    @Override
    public Collection<String> getKeys() {
        return Collections.unmodifiableCollection(replacements.keySet());
    }

    @Override
    public Collection<String> getValues(String key) {
        return replacements.getOrDefault(key, Collections.emptyList());
    }

    @Override
    public void forEach(BiConsumer<String, Collection<String>> consumer) {
        replacements.forEach(consumer);
    }

    public static class Serializer extends Config.Serializer<PlaceholdersConfig> {

        @Override
        public PlaceholdersConfig deserialize(String name, JsonObject json) {
            // noinspection SizeReplaceableByIsEmpty
            if (json.size() == 0) { // json.isEmpty crashes in prod...
                setInvalid();
                return new PlaceholdersConfig(name, Defaults.PLACEHOLDERS);
            }

            Map<String, Collection<String>> replacements = safeGet(() -> {
                ImmutableMap.Builder<String, Collection<String>> builder = ImmutableMap.builder();
                for (var entry : json.entrySet()) {
                    ImmutableSet.Builder<String> placeholders = ImmutableSet.builder();
                    for (JsonElement element : entry.getValue().getAsJsonArray()) {
                        if (element.isJsonPrimitive()) {
                            placeholders.add(element.getAsString().trim());
                        }
                    }

                    builder.put(entry.getKey().trim(), placeholders.build());
                }

                return builder.build();
            }, Defaults.PLACEHOLDERS);

            return new PlaceholdersConfig(name, replacements);
        }

        @Override
        public JsonObject serialize(PlaceholdersConfig src) {
            JsonObject json = new JsonObject();
            for (var entry : src.replacements.entrySet()) {
                json.add(entry.getKey(), JsonUtils.toArray(entry.getValue()));
            }

            return json;
        }
    }
}

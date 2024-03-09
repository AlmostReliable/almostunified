package com.almostreliable.unified.config;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.api.Replacements;
import com.almostreliable.unified.utils.JsonUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class ReplacementsConfig extends Config implements Replacements {

    public static final String NAME = "replacements";
    private final Map<String, Collection<String>> replacements;

    public ReplacementsConfig(Map<String, Collection<String>> replacements) {
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
            AlmostUnified.LOG.warn(
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

    public static class Serializer extends Config.Serializer<ReplacementsConfig> {

        @Override
        public ReplacementsConfig deserialize(JsonObject json) {
            //noinspection SizeReplaceableByIsEmpty
            if (json.size() == 0) { // json.isEmpty crashes in prod...
                setInvalid();
                return new ReplacementsConfig(Defaults.REPLACEMENTS);
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
            }, Defaults.REPLACEMENTS);

            return new ReplacementsConfig(replacements);
        }

        @Override
        public JsonObject serialize(ReplacementsConfig src) {
            JsonObject json = new JsonObject();
            for (var entry : src.replacements.entrySet()) {
                json.add(entry.getKey(), JsonUtils.toArray(entry.getValue()));
            }

            return json;
        }
    }
}

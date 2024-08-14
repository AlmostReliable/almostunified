package com.almostreliable.unified.config;

import com.almostreliable.unified.api.Placeholders;
import com.almostreliable.unified.utils.JsonUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

public final class PlaceholderConfig extends Config implements Placeholders {

    public static final String NAME = "placeholders";
    public static final PlaceholderSerializer SERIALIZER = new PlaceholderSerializer();

    private final Map<String, Collection<String>> placeholders;

    private PlaceholderConfig(Map<String, Collection<String>> placeholders) {
        super(NAME);
        this.placeholders = placeholders;
    }

    @Override
    public Collection<String> apply(String str) {
        AtomicReference<Collection<String>> inflated = new AtomicReference<>(new HashSet<>());
        inflated.get().add(str);
        forEach((placeholder, replacements) -> inflated.set(inflate(inflated.get(), placeholder, replacements)));
        return inflated.get();
    }

    @Override
    public Collection<String> getPlaceholders() {
        return Collections.unmodifiableCollection(placeholders.keySet());
    }

    @Override
    public Collection<String> getReplacements(String placeholder) {
        return placeholders.getOrDefault(placeholder, Collections.emptyList());
    }

    @Override
    public void forEach(BiConsumer<String, Collection<String>> consumer) {
        placeholders.forEach(consumer);
    }

    private static Collection<String> inflate(Collection<String> values, String placeholder, Collection<String> replacements) {
        String formattedPlaceholder = "{" + placeholder + "}";
        Set<String> result = new HashSet<>();

        for (String value : values) {
            for (String replacement : replacements) {
                result.add(value.replace(formattedPlaceholder, replacement));
            }
        }

        return result;
    }

    public static final class PlaceholderSerializer extends Config.Serializer<PlaceholderConfig> {

        private PlaceholderSerializer() {}

        @Override
        public PlaceholderConfig handleDeserialization(JsonObject json) {
            // noinspection SizeReplaceableByIsEmpty
            if (json.size() == 0) { // json.isEmpty crashes in prod...
                setInvalid();
                return new PlaceholderConfig(Defaults.PLACEHOLDERS);
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

            return new PlaceholderConfig(replacements);
        }

        @Override
        public JsonObject serialize(PlaceholderConfig config) {
            JsonObject json = new JsonObject();
            for (var entry : config.placeholders.entrySet()) {
                json.add(entry.getKey(), JsonUtils.toArray(entry.getValue()));
            }

            return json;
        }
    }
}

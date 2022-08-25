package com.almostreliable.unified.config;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.AlmostUnifiedPlatform;
import com.almostreliable.unified.utils.JsonUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Config {

    public static <T extends Config> T load(String name, Serializer<T> serializer) {
        JsonObject json = safeLoadJson(name);
        T config = serializer.deserialize(json);
        if (serializer.isInvalid()) {
            AlmostUnified.LOG.warn("Config {} is invalid or does not exist. Saving new config", name);
            save(name, config, serializer);
        }
        return config;
    }

    public static <T extends Config> void save(String name, T config, Serializer<T> serializer) {
        JsonObject json = serializer.serialize(config);
        Path p = createConfigDir();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonString = gson.toJson(json);
        try {
            Files.writeString(buildPath(p, name),
                    jsonString,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE);
        } catch (IOException e) {
            AlmostUnified.LOG.error(e);
        }
    }

    private static JsonObject safeLoadJson(String file) {
        Path p = createConfigDir();
        try (BufferedReader reader = Files.newBufferedReader(buildPath(p, file))) {
            return new Gson().fromJson(reader, JsonObject.class);
        } catch (Exception ignored) {
        }
        return new JsonObject();
    }

    private static Path createConfigDir() {
        Path p = AlmostUnifiedPlatform.INSTANCE.getConfigPath();
        try {
            Files.createDirectories(p);
        } catch (IOException e) {
            AlmostUnified.LOG.error("Failed to create config directory", e);
        }
        return p;
    }

    private static Path buildPath(Path p, String name) {
        return p.resolve(name + ".json");
    }

    public abstract static class Serializer<T extends Config> {
        private boolean valid = true;

        protected void setInvalid() {
            this.valid = false;
        }

        public boolean isInvalid() {
            return !valid;
        }

        public <V> V safeGet(Supplier<V> supplier, V defaultValue) {
            try {
                return supplier.get();
            } catch (Exception e) {
                setInvalid();
            }
            return defaultValue;
        }

        protected Set<ResourceLocation> deserializeResourceLocations(JsonObject json, String configKey, List<String> defaultValue) {
            return safeGet(() -> JsonUtils
                            .toList(json.getAsJsonArray(configKey))
                            .stream()
                            .map(ResourceLocation::new)
                            .collect(Collectors.toSet()),
                    new HashSet<>(defaultValue.stream().map(ResourceLocation::new).toList()));
        }

        protected void serializeResourceLocations(JsonObject json, String configKey, Set<ResourceLocation> resourceLocations) {
            json.add(configKey,
                    JsonUtils.toArray(resourceLocations
                            .stream()
                            .map(ResourceLocation::toString)
                            .toList()));
        }

        public abstract T deserialize(JsonObject json);

        public abstract JsonObject serialize(T src);
    }
}

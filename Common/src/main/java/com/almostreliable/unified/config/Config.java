package com.almostreliable.unified.config;

import com.almostreliable.unified.AlmostUnifiedCommon;
import com.almostreliable.unified.AlmostUnifiedPlatform;
import com.almostreliable.unified.api.constant.ModConstants;
import com.almostreliable.unified.utils.JsonUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Config {

    private static final String CONFIG_DIR_PROPERTY = ModConstants.ALMOST_UNIFIED + ".configDir";
    private final String name;

    Config(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static <T extends Config> T load(String name, Serializer<T> serializer) {
        AlmostUnifiedCommon.LOGGER.info("Loading config '{}.json'.", name);

        JsonObject json = JsonUtils.safeReadFromFile(buildPath(createConfigDir(), name), new JsonObject());
        T config = serializer.deserialize(json);

        if (serializer.isInvalid()) {
            save(buildPath(createConfigDir(), config.getName()), config, serializer);
        }

        return config;
    }

    static Path createConfigDir() {
        Path path = AlmostUnifiedPlatform.INSTANCE.getConfigPath();
        String property = System.getProperty(CONFIG_DIR_PROPERTY);
        if (property != null) {
            path = Path.of(property);
        }

        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            AlmostUnifiedCommon.LOGGER.error("Failed to create config directory.", e);
        }

        return path;
    }

    static <T extends Config> void save(Path path, T config, Serializer<T> serializer) {
        if (Files.exists(path)) {
            backupConfig(path);
        } else {
            AlmostUnifiedCommon.LOGGER.warn("Config '{}.json' not found. Creating default config.", config.getName());
        }

        JsonObject json = serializer.serialize(config);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonString = gson.toJson(json);
        try {
            Files.writeString(
                path,
                jsonString,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE
            );
        } catch (IOException e) {
            AlmostUnifiedCommon.LOGGER.error("Failed to save config '{}'.", config.getName(), e);
        }
    }

    private static void backupConfig(Path path) {
        AlmostUnifiedCommon.LOGGER.warn("Config '{}' is invalid. Backing up and recreating.", path.getFileName());

        Path backupPath = path.resolveSibling(path.getFileName() + ".bak");
        try {
            Files.deleteIfExists(backupPath);
            Files.move(path, backupPath);
        } catch (IOException e) {
            AlmostUnifiedCommon.LOGGER.error("Config '{}' could not be backed up.", path.getFileName(), e);
        }
    }

    private static Path buildPath(Path path, String name) {
        return path.resolve(name + ".json");
    }

    public abstract static class Serializer<T extends Config> {

        private boolean valid;

        T deserialize(JsonObject json) {
            valid = true;
            return handleDeserialization(json);
        }

        abstract T handleDeserialization(JsonObject json);

        abstract JsonObject serialize(T config);

        <V> V safeGet(Supplier<V> supplier, V defaultValue) {
            try {
                return supplier.get();
            } catch (Exception e) {
                setInvalid();
                return defaultValue;
            }
        }

        void setInvalid() {
            this.valid = false;
        }

        boolean isInvalid() {
            return !valid;
        }

        Set<Pattern> deserializePatterns(JsonObject json, String configKey, List<String> defaultValue) {
            return safeGet(
                () -> JsonUtils
                    .toList(json.getAsJsonArray(configKey))
                    .stream()
                    .map(Pattern::compile)
                    .collect(Collectors.toSet()),
                new HashSet<>(defaultValue.stream().map(Pattern::compile).toList())
            );
        }

        void serializePatterns(JsonObject json, String configKey, Set<Pattern> patterns) {
            json.add(configKey, JsonUtils.toArray(patterns.stream().map(Pattern::pattern).toList()));
        }
    }
}

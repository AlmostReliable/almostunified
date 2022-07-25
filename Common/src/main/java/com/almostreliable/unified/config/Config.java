package com.almostreliable.unified.config;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.AlmostUnifiedPlatform;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Supplier;

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
        try {
            Path p = createConfigDir();
            BufferedReader bufferedReader = Files.newBufferedReader(buildPath(p, file));
            return new Gson().fromJson(bufferedReader, JsonObject.class);
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

    public static abstract class Serializer<T extends Config> {
        private boolean invalid = false;

        protected void setInvalid() {
            this.invalid = true;
        }

        public boolean isInvalid() {
            return invalid;
        }

        public <V> V safeGet(Supplier<V> supplier, V defaultValue) {
            try {
                return supplier.get();
            } catch (Exception e) {
                setInvalid();
            }
            return defaultValue;
        }

        public abstract T deserialize(JsonObject json);

        public abstract JsonObject serialize(T src);
    }
}

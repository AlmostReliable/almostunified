package com.almostreliable.unified.utils;

import com.almostreliable.unified.AlmostUnifiedPlatform;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class RecipeTypePropertiesLogger {
    private final Map<String, List<String>> properties = new HashMap<>();

    private List<String> getProperties(String mod) {
        return properties.computeIfAbsent(mod, $ -> new ArrayList<>());
    }

    public void log(ResourceLocation recipeType, JsonObject recipe, Collection<String> keys) {
        String mod = recipeType.getNamespace();
        recipe.entrySet().forEach(e -> getProperties(mod).add(e.getKey()));
    }

    public void log(String mod, String property) {
        getProperties(mod).add(property);
    }

    public void writeFile() {
        StringBuilder sb = new StringBuilder();
        properties.forEach((mod, properties) -> {
            sb.append(mod).append(":\n");
            properties.sort(String::compareTo);
            properties.forEach(property -> sb.append("    ").append(property).append("\n"));
        });

        Path path = AlmostUnifiedPlatform.INSTANCE.getLogPath();
        try {
            Files.createDirectories(path);
            Path filePath = path.resolve("debug_recipe_properties.txt");
            Files.writeString(filePath,
                    sb.toString(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

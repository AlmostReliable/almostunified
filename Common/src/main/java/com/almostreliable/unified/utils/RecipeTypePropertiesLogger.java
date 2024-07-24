package com.almostreliable.unified.utils;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeTypePropertiesLogger {
    private final Map<String, List<String>> properties = new HashMap<>();

    private List<String> getProperties(String mod) {
        return properties.computeIfAbsent(mod, $ -> new ArrayList<>());
    }

    public void log(ResourceLocation recipeType, JsonObject recipe) {
        String mod = recipeType.getNamespace();
        recipe.entrySet().forEach(e -> getProperties(mod).add(e.getKey()));
    }

    public void log(String mod, String property) {
        getProperties(mod).add(property);
    }

    public void writeFile() {
        StringBuilder sb = new StringBuilder();
        properties.forEach((mod, props) -> {
            sb.append(mod).append(":\n");
            props.sort(String::compareTo);
            props.forEach(property -> sb.append("    ").append(property).append("\n"));
        });

        FileUtils.writeLog("debug_recipe_properties.txt", stringBuilder -> sb.append(sb));
    }
}

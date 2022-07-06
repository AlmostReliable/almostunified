package com.almostreliable.unified.recipe;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class RecipeTransformationResult {
    private final Table<ResourceLocation, ResourceLocation, Entry> allTrackedRecipes = HashBasedTable.create();
    private final Table<ResourceLocation, ResourceLocation, Entry> transformedRecipes = HashBasedTable.create();
    private final long startTime;
    private long finishTime;

    public RecipeTransformationResult() {
        this.startTime = System.currentTimeMillis();
    }

    public void track(ResourceLocation recipe, JsonObject json, @Nullable JsonObject result) {
        Entry entry = new Entry(json, result);
        ResourceLocation type = entry.getType();

        if (allTrackedRecipes.contains(type, recipe)) {
            throw new IllegalArgumentException("Already tracking " + type + ":" + recipe);
        }
        allTrackedRecipes.put(type, recipe, entry);

        if (entry.isTransformed()) {
            transformedRecipes.put(type, recipe, entry);
        }
    }

    public void forEachTransformedRecipe(BiConsumer<ResourceLocation, Map<ResourceLocation, Entry>> consumer) {
        transformedRecipes
                .rowMap()
                .entrySet()
                .stream()
                .sorted(Comparator.comparing(o -> o.getKey().toString()))
                .forEach(e -> consumer.accept(e.getKey(), e.getValue()));
    }

    public boolean isEnded() {
        return finishTime != 0;
    }

    public void end() {
        if (isEnded()) {
            throw new IllegalStateException("Already ended");
        }
        this.finishTime = System.currentTimeMillis();
    }

    public int getRecipeCount() {
        return allTrackedRecipes.columnKeySet().size();
    }

    public Map<ResourceLocation, Entry> getAllEntriesByType(ResourceLocation type) {
        Map<ResourceLocation, Entry> entry = allTrackedRecipes.rowMap().getOrDefault(type, new HashMap<>());
        return Collections.unmodifiableMap(entry);
    }

    public int getTransformedCount() {
        return transformedRecipes.columnKeySet().size();
    }

    public long getStartTime() {
        return startTime;
    }

    public double getTotalTime() {
        return isEnded() ? (this.finishTime - this.startTime) : 0;
    }

    public record Entry(JsonObject originalRecipe, @Nullable JsonObject transformedRecipe) {
        public boolean isTransformed() {
            return transformedRecipe != null;
        }

        public ResourceLocation getType() {
            if (originalRecipe.get("type") instanceof JsonPrimitive type) {
                return ResourceLocation.tryParse(type.getAsString());
            }

            throw new IllegalArgumentException("No type found in recipe");
        }
    }
}

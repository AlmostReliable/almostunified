package com.almostreliable.unified.recipe;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.recipe.handler.RecipeHandlerFactory;
import com.almostreliable.unified.utils.ReplacementMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Map;

public class RecipeTransformer {

    private final RecipeHandlerFactory factory;
    private final ReplacementMap replacementMap;

    public RecipeTransformer(RecipeHandlerFactory factory, ReplacementMap replacementMap) {
        this.factory = factory;
        this.replacementMap = replacementMap;
    }

    public void transformRecipes(Map<ResourceLocation, JsonElement> recipes) {
        Multimap<ResourceLocation, ResourceLocation> typeCount = HashMultimap.create();

        int transformedRecipes = 0;
        long start = System.nanoTime();
        for (var entry : recipes.entrySet()) {
            if (entry.getValue() instanceof JsonObject json) {
                JsonObject transformedJson = transformRecipe(json);
                if (transformedJson != null) {
                    transformedRecipes++;
                    entry.setValue(transformedJson);

                    // TODO for debugging remove this later
                    ResourceLocation recipeType = getRecipeType(json);
                    typeCount.put(recipeType, entry.getKey());
                }
            }
        }
        long finish = System.nanoTime();
        long timeElapsed = finish - start;
        AlmostUnified.LOG.info("Transformed {}/{} recipes changes in {}ms",
                transformedRecipes,
                recipes.size(),
                timeElapsed / 1000_000D);
        // TODO Pls remove this on release
        typeCount.asMap().entrySet().stream().sorted(Comparator.comparing(o -> o.getKey().toString())).forEach((e) -> {
            AlmostUnified.LOG.info("{}: {} | {}",
                    StringUtils.leftPad(e.getKey().toString(), 40),
                    StringUtils.leftPad(String.valueOf(e.getValue().size()), 4),
                    e.getValue().stream().map(rl -> "\"" + rl.toString() + "\"").toList().toString());
        });
    }

    @Nullable
    public JsonObject transformRecipe(JsonObject json) {
        ResourceLocation recipeType = getRecipeType(json);
        if (recipeType == null) {
            return null;
        }

        try {
            RecipeContextImpl ctx = new RecipeContextImpl(recipeType, json, replacementMap);
            RecipeTransformationsImpl builder = new RecipeTransformationsImpl();
            factory.create(builder, ctx);
            JsonObject result = builder.transform(json, ctx);
            if (result != null) {
                return result;
            }
        } catch (Exception e) {
            AlmostUnified.LOG.warn("Error transforming recipe type '{}': {}",
                    recipeType,
                    e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    @Nullable
    protected ResourceLocation getRecipeType(JsonObject recipeJson) {
        if (recipeJson.get("type") instanceof JsonPrimitive type) {
            return ResourceLocation.tryParse(type.getAsString());
        }

        return null;
    }
}

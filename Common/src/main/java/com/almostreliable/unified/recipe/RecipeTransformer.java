package com.almostreliable.unified.recipe;

import com.almostreliable.unified.AlmostUnified;
import com.almostreliable.unified.recipe.handler.RecipeHandlerFactory;
import com.almostreliable.unified.utils.ReplacementMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Map;

public class RecipeTransformer {

    private final RecipeHandlerFactory factory;
    private final ReplacementMap replacementMap;

    public RecipeTransformer(RecipeHandlerFactory factory, ReplacementMap replacementMap) {
        this.factory = factory;
        this.replacementMap = replacementMap;
    }

    public boolean hasValidType(JsonObject json) {
        if (json.get("type") instanceof JsonPrimitive type) {
            return ResourceLocation.tryParse(type.getAsString()) != null;
        }
        return false;
    }

    public RecipeTransformationResult transformRecipes(Map<ResourceLocation, JsonElement> recipes) {
        RecipeTransformationResult recipeTransformationResult = new RecipeTransformationResult();

        for (var entry : recipes.entrySet()) {
            if (!hasValidType(entry.getValue().getAsJsonObject())) {
                continue;
            }

            if (entry.getValue() instanceof JsonObject json) {
                JsonObject result = transformRecipe(entry.getKey(), json);
                recipeTransformationResult.track(entry.getKey(), json, result);
                if (result != null) {
                    entry.setValue(result);
                }
            }
        }

        recipeTransformationResult.end();
        return recipeTransformationResult;
    }

    @Nullable
    public JsonObject transformRecipe(ResourceLocation recipeId, JsonObject json) {
        try {
            RecipeContextImpl ctx = new RecipeContextImpl(json, replacementMap);
            RecipeTransformationBuilderImpl builder = new RecipeTransformationBuilderImpl();
            factory.fillTransformations(builder, ctx);
            JsonObject result = builder.transform(json, ctx);
            if (result != null) {
                return result;
            }
        } catch (Exception e) {
            AlmostUnified.LOG.warn("Error transforming recipe '{}': {}",
                    recipeId,
                    e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}

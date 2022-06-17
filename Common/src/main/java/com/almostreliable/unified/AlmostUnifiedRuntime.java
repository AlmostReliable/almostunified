package com.almostreliable.unified;

import com.almostreliable.unified.api.RecipeTransformContext;
import com.almostreliable.unified.api.RecipeTransformer;
import com.almostreliable.unified.api.RecipeTransformerFactory;
import com.almostreliable.unified.api.RecipeTransformerRegistry;
import com.almostreliable.unified.transformer.GenericRecipeTransformerFactory;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagManager;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlmostUnifiedRuntime {

    protected final ModConfig config;
    protected final RecipeTransformerFactory defaultFactory = new GenericRecipeTransformerFactory();
    @Nullable protected TagManager tagManager;

    public AlmostUnifiedRuntime() {
        config = new ModConfig(BuildConfig.MOD_ID);
    }

    public void run(Map<ResourceLocation, JsonElement> recipes) {
        config.load();
        RecipeTransformContext helper = createContext(config.getAllowedTags(), config.getModPriorities());
//        transformRecipes(recipes, helper);
    }

    public void transformRecipes(Map<ResourceLocation, JsonElement> recipes, RecipeTransformContext helper) {
        int transformedRecipes = 0;
        int transformedPropertiesInRecipes = 0;
        long start = System.nanoTime();
        for (var entry : recipes.entrySet()) {
            if (entry.getValue() instanceof JsonObject json) {
                int changes = transformRecipe(json, helper);
                if (changes > 0) {
                    transformedRecipes++;
                    transformedPropertiesInRecipes += changes;
                }
            }
        }
        long finish = System.nanoTime();
        long timeElapsed = finish - start;
        AlmostUnified.LOG.info("Transformed {}/{} recipes with {} changes in {}ms",
                transformedRecipes,
                recipes.size(),
                transformedPropertiesInRecipes,
                timeElapsed / 1000_000D);
    }

    public int transformRecipe(JsonObject json, RecipeTransformContext helper) {
        ResourceLocation recipeType = getRecipeType(json);
        if (recipeType == null) {
            return 0;
        }

        RecipeTransformerFactory factory = RecipeTransformerRegistry.getOrDefault(recipeType, defaultFactory);
        int transformedProperties = 0;
        for (var entry : json.entrySet()) {
            String property = entry.getKey();
            RecipeTransformer recipeTransformer = factory.create(recipeType, property);
            if (recipeTransformer == null) {
                continue;
            }

            try {
                JsonElement jsonValue = json.get(property);
                JsonElement overriddenJson = recipeTransformer.transformRecipe(jsonValue.deepCopy(), helper);
                if (overriddenJson != null && !jsonValue.equals(overriddenJson)) {
                    entry.setValue(overriddenJson);
                    transformedProperties++;
                }
            } catch (Exception e) {
                AlmostUnified.LOG.warn("Error transforming recipe for type '{}' with property '{}': {}",
                        recipeType,
                        property,
                        e.getMessage());
                e.printStackTrace();
            }
        }
        return transformedProperties;
    }


    @Nullable
    protected ResourceLocation getRecipeType(JsonObject recipeJson) {
        String type = recipeJson.get("type").getAsString();
        return ResourceLocation.tryParse(type);
    }

    public void updateTagManager(TagManager tagManager) {
        this.tagManager = tagManager;
    }

    protected RecipeTransformContext createContext(List<ResourceLocation> allowedTags, List<String> modPriorities) {
        if (tagManager == null) {
            throw new IllegalStateException("Internal error. TagManager was not updated correctly");
        }


        TagMap tagMap = TagMap.create(tagManager);
        Map<ResourceLocation, ResourceLocation> itemToTagMapping = new HashMap<>(allowedTags.size());

        for (ResourceLocation tag : allowedTags) {
            Collection<ResourceLocation> items = tagMap.getItems(tag);
            for (ResourceLocation item : items) {
                if (itemToTagMapping.containsKey(item)) {
                    AlmostUnified.LOG.warn("Item '{}' already has a tag '{}' for recipe replacement. Skipping this tag",
                            item,
                            tag);
                    continue;
                }

                itemToTagMapping.put(item, tag);
            }
        }

        return new RecipeTransformContextImpl(tagMap, itemToTagMapping, modPriorities);
    }
}

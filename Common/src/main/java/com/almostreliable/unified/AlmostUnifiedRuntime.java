package com.almostreliable.unified;

import com.almostreliable.unified.api.RecipeHandler;
import com.almostreliable.unified.handler.RecipeHandlerFactory;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagManager;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.*;

public abstract class AlmostUnifiedRuntime {

    protected final ModConfig config;
    protected final RecipeHandlerFactory recipeHandlerFactory;
    @Nullable protected TagManager tagManager;
    protected List<String> modPriorities = new ArrayList<>();

    public AlmostUnifiedRuntime(RecipeHandlerFactory recipeHandlerFactory) {
        this.recipeHandlerFactory = recipeHandlerFactory;
        config = new ModConfig(BuildConfig.MOD_ID);
    }

    public void run(Map<ResourceLocation, JsonElement> recipes) {
        config.load();
        modPriorities = config.getModPriorities();
        onRun();
        ReplacementMap replacementMap = createContext(config.getAllowedTags(), modPriorities);
        transformRecipes(recipes, replacementMap);
    }

    public void transformRecipes(Map<ResourceLocation, JsonElement> recipes, ReplacementMap replacementMap) {
        int transformedRecipes = 0;
        long start = System.nanoTime();
        for (var entry : recipes.entrySet()) {
            if (entry.getValue() instanceof JsonObject json) {
                JsonObject transformedJson = transformRecipe(entry.getKey(), json, replacementMap);
                if (transformedJson != null) {
                    transformedRecipes++;
                    entry.setValue(transformedJson);
                }
            }
        }
        long finish = System.nanoTime();
        long timeElapsed = finish - start;
        AlmostUnified.LOG.info("Transformed {}/{} recipes changes in {}ms",
                transformedRecipes,
                recipes.size(),
                timeElapsed / 1000_000D);
    }

    @Nullable
    public JsonObject transformRecipe(ResourceLocation id, JsonObject json, ReplacementMap replacementMap) {
        ResourceLocation recipeType = getRecipeType(json);
        if (recipeType == null) {
            return null;
        }

        RecipeContextImpl ctx = new RecipeContextImpl(recipeType, id, json, replacementMap);

        try {
            RecipeHandler recipeHandler = recipeHandlerFactory.create(ctx);
            if (recipeHandler == null) {
                return null;
            }

            JsonObject copy = json.deepCopy();
            recipeHandler.transformRecipe(copy, ctx);
            if (!json.equals(copy)) {
                if (ctx.getType().getNamespace().equals("immersiveengineering")) {
                    AlmostUnified.LOG.info("Transformed recipe '{}' for type '{}' ========> {}",
                            id,
                            recipeType,
                            copy);
                }

                return copy;
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
        String type = recipeJson.get("type").getAsString();
        return ResourceLocation.tryParse(type);
    }

    public void updateTagManager(TagManager tagManager) {
        this.tagManager = tagManager;
    }

    protected ReplacementMap createContext(List<TagKey<Item>> allowedTags, List<String> modPriorities) {
        if (tagManager == null) {
            throw new IllegalStateException("Internal error. TagManager was not updated correctly");
        }


        TagMap tagMap = TagMap.create(tagManager);
        Map<ResourceLocation, TagKey<Item>> itemToTagMapping = new HashMap<>(allowedTags.size());

        for (TagKey<Item> tag : allowedTags) {
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

        return new ReplacementMap(tagMap, itemToTagMapping, modPriorities);
    }

    protected abstract void onRun();
}

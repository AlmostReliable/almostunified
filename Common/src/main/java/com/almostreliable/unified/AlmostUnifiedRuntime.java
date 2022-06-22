package com.almostreliable.unified;

import com.almostreliable.unified.handler.RecipeHandlerFactory;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagManager;
import net.minecraft.world.item.Item;
import org.apache.commons.lang3.StringUtils;

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
        Multimap<ResourceLocation, ResourceLocation> typeCount = HashMultimap.create();

        int transformedRecipes = 0;
        long start = System.nanoTime();
        for (var entry : recipes.entrySet()) {
            if (entry.getValue() instanceof JsonObject json) {
                JsonObject transformedJson = transformRecipe(entry.getKey(), json, replacementMap);
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
                    e.getValue().toString());
        });
    }

    @Nullable
    public JsonObject transformRecipe(ResourceLocation id, JsonObject json, ReplacementMap replacementMap) {
        ResourceLocation recipeType = getRecipeType(json);
        if (recipeType == null) {
            return null;
        }

        try {
            RecipeContextImpl ctx = new RecipeContextImpl(recipeType, id, json, replacementMap);
            RecipeTransformationsImpl builder = new RecipeTransformationsImpl();
            recipeHandlerFactory.create(builder, ctx);
            JsonObject copy = json.deepCopy();
            builder.transform(copy, ctx);
            if (!json.equals(copy)) {
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

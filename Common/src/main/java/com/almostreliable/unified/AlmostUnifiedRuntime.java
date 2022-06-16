package com.almostreliable.unified;

import com.almostreliable.unified.api.RecipeTransformer;
import com.almostreliable.unified.api.RecipeTransformerFactory;
import com.almostreliable.unified.api.RecipeTransformerRegistry;
import com.almostreliable.unified.api.ReplacementLookupHelper;
import com.almostreliable.unified.transformer.GenericRecipeTransformer;
import com.almostreliable.unified.transformer.GenericRecipeTransformerFactory;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagManager;
import org.apache.commons.lang3.time.StopWatch;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlmostUnifiedRuntime {

    protected final ModConfig config;
    protected final RecipeTransformer defaultTransformer = new GenericRecipeTransformer();
    protected final RecipeTransformerFactory defaultFactory = new GenericRecipeTransformerFactory();
    @Nullable protected TagManager tagManager;

    public AlmostUnifiedRuntime() {
        config = new ModConfig(BuildConfig.MOD_ID);
    }

    public void run(Map<ResourceLocation, JsonElement> recipes) {
        config.load();
        ReplacementLookupHelper helper = createHelper(config.getAllowedTags(), config.getModPriorities());
        transformRecipes(recipes, helper);
    }

    public void transformRecipes(Map<ResourceLocation, JsonElement> recipes, ReplacementLookupHelper helper) {
        int transformedRecipes = 0;
        int transformedPropertiesInRecipes = 0;
        long start = System.nanoTime();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
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
        stopWatch.stop();
        AlmostUnified.LOG.info("Transformed {}/{} recipes with {} changes in {}ms",
                transformedRecipes,
                recipes.size(),
                transformedPropertiesInRecipes,
                timeElapsed / 1000_000D);
    }

    public int transformRecipe(JsonObject json, ReplacementLookupHelper helper) {
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

    protected ReplacementLookupHelper createHelper(List<ResourceLocation> allowedTags, List<String> modPriorities) {
        if (tagManager == null) {
            throw new IllegalStateException("Internal error. TagManager was not updated correctly");
        }

        var tags = tagManager
                .getResult()
                .stream()
                .filter(result -> result.key().equals(Registry.ITEM_REGISTRY))
                .findFirst()
                .map(TagManager.LoadResult::tags)
                .orElseThrow(() -> new IllegalStateException("No item tag result found"));

        Map<ResourceLocation, ResourceLocation> itemTagMap = new HashMap<>();
        Multimap<ResourceLocation, ResourceLocation> tagToEntry = HashMultimap.create();

        for (ResourceLocation allowedTag : allowedTags) {
            Tag<? extends Holder<?>> holderTag = tags.get(allowedTag);
            if (holderTag == null) {
                continue;
            }

            for (Holder<?> holder : holderTag.getValues()) {
                ResourceLocation itemId = holder.unwrapKey().map(ResourceKey::location).orElse(null);
                if (itemTagMap.containsKey(itemId)) {
                    AlmostUnified.LOG.warn("Item '{}' already has a tag '{}' for recipe replacement. Skipping this tag",
                            itemId,
                            allowedTag);
                    continue;
                }

                itemTagMap.put(itemId, allowedTag);
                tagToEntry.put(allowedTag, itemId);
            }
        }

        return new LookupByModPriority(itemTagMap, tagToEntry, modPriorities);
    }
}

package com.almostreliable.unitagged;

import com.almostreliable.unitagged.api.RecipeTransformer;
import com.almostreliable.unitagged.api.RecipeTransformers;
import com.almostreliable.unitagged.api.ReplacementLookupHelper;
import com.almostreliable.unitagged.transformer.GenericRecipeTransformer;
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

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlmostUnifiedRuntime {

    protected final ModConfig config;
    protected final RecipeTransformer defaultTransformer = new GenericRecipeTransformer();
    @Nullable protected TagManager tagManager;

    public AlmostUnifiedRuntime() {
        config = new ModConfig("almostunified");
    }

    public void run(Map<ResourceLocation, JsonElement> recipes) {
        config.load();
        ReplacementLookupHelper helper = createHelper(config.getAllowedTags(), config.getModPriorities());
        transformRecipes(recipes, helper);
    }

    public void transformRecipes(Map<ResourceLocation, JsonElement> recipes, ReplacementLookupHelper helper) {
        for (var entry : recipes.entrySet()) {
            if (entry.getValue() instanceof JsonObject json) {
                transformRecipe(json, helper);
            }
        }
    }

    public void transformRecipe(JsonObject json, ReplacementLookupHelper helper) {
        ResourceLocation recipeType = getRecipeType(json);
        if (recipeType == null) {
            return;
        }

        RecipeTransformer transformer = RecipeTransformers.getOrDefault(recipeType, defaultTransformer);
        transformer.transformRecipe(json, helper);
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
            if(holderTag == null) {
                continue;
            }

            for (Holder<?> holder : holderTag.getValues()) {
                ResourceLocation itemId = holder.unwrapKey().map(ResourceKey::location).orElse(null);
                if(itemTagMap.containsKey(itemId)) {
                    UniTagged.LOG.warn("Item '{}' already has a tag '{}' for recipe replacement. Skipping this tag", itemId, allowedTag);
                    continue;
                }

                itemTagMap.put(itemId, allowedTag);
                tagToEntry.put(allowedTag, itemId);
            }
        }

        return new LookupByModPriority(itemTagMap, tagToEntry, modPriorities);
    }
}

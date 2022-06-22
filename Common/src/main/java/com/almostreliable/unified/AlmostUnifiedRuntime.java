package com.almostreliable.unified;

import com.almostreliable.unified.recipe.RecipeTransformer;
import com.almostreliable.unified.recipe.handler.RecipeHandlerFactory;
import com.almostreliable.unified.utils.ReplacementMap;
import com.almostreliable.unified.utils.TagMap;
import com.google.gson.JsonElement;
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
        RecipeTransformer transformer = new RecipeTransformer(recipeHandlerFactory, replacementMap);
        transformer.transformRecipes(recipes);
    }

    public void updateTagManager(TagManager tagManager) {
        this.tagManager = tagManager;
    }

    protected TagMap createTagMap() {
        if (tagManager == null) {
            throw new IllegalStateException("Internal error. TagManager was not updated correctly");
        }

        return TagMap.create(tagManager);
    }

    protected ReplacementMap createContext(List<TagKey<Item>> allowedTags, List<String> modPriorities) {
        TagMap tagMap = createTagMap();
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

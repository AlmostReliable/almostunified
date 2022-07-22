package com.almostreliable.unified;

import com.almostreliable.unified.recipe.RecipeDumper;
import com.almostreliable.unified.recipe.RecipeTransformer;
import com.almostreliable.unified.recipe.handler.RecipeHandlerFactory;
import com.almostreliable.unified.utils.ReplacementMap;
import com.almostreliable.unified.utils.TagMap;
import com.almostreliable.unified.utils.UnifyTag;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagManager;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        List<UnifyTag<Item>> allowedTags = config.getAllowedTags();
        TagMap tagMap = createTagMap(allowedTags);
        ReplacementMap replacementMap = new ReplacementMap(tagMap, modPriorities, config.getStoneStrata());

        long startTime = System.currentTimeMillis();
        RecipeTransformer.Result result = new RecipeTransformer(recipeHandlerFactory, replacementMap)
                .transformRecipes(recipes);
        long endTime = System.currentTimeMillis();

        new RecipeDumper(result, startTime, endTime).dump();
    }

    public void updateTagManager(TagManager tagManager) {
        this.tagManager = tagManager;
    }

    protected TagMap createTagMap(List<UnifyTag<Item>> allowedTags) {
        if (tagManager == null) {
            throw new IllegalStateException("Internal error. TagManager was not updated correctly");
        }

        return TagMap.create(tagManager, allowedTags::contains);
    }

    protected abstract void onRun();
}

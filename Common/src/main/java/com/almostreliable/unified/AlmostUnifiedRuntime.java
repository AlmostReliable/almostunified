package com.almostreliable.unified;

import com.almostreliable.unified.config.Config;
import com.almostreliable.unified.config.DuplicationConfig;
import com.almostreliable.unified.config.UnifyConfig;
import com.almostreliable.unified.recipe.RecipeDumper;
import com.almostreliable.unified.recipe.RecipeTransformer;
import com.almostreliable.unified.recipe.unifier.RecipeHandlerFactory;
import com.almostreliable.unified.utils.ReplacementMap;
import com.almostreliable.unified.utils.TagMap;
import com.almostreliable.unified.utils.UnifyTag;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagManager;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class AlmostUnifiedRuntime {
    protected final RecipeHandlerFactory recipeHandlerFactory;
    @Nullable protected TagManager tagManager;

    public AlmostUnifiedRuntime(RecipeHandlerFactory recipeHandlerFactory) {
        this.recipeHandlerFactory = recipeHandlerFactory;
    }

    public void run(Map<ResourceLocation, JsonElement> recipes) {
        DuplicationConfig dupConfig = Config.load(DuplicationConfig.NAME, new DuplicationConfig.Serializer());
        UnifyConfig unifyConfig = Config.load(UnifyConfig.NAME, new UnifyConfig.Serializer());

        List<UnifyTag<Item>> allowedTags = unifyConfig.bakeTags();
        TagMap tagMap = createTagMap(allowedTags);
        ReplacementMap replacementMap = new ReplacementMap(tagMap, unifyConfig);

        long startTime = System.currentTimeMillis();
        RecipeTransformer.Result result = new RecipeTransformer(recipeHandlerFactory,
                replacementMap,
                unifyConfig,
                dupConfig).transformRecipes(recipes);
        new RecipeDumper(result, startTime, System.currentTimeMillis()).dump();
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
}

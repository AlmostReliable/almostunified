package com.almostreliable.unified;

import com.almostreliable.unified.api.ReplacementMap;
import com.almostreliable.unified.config.DebugConfig;
import com.almostreliable.unified.config.DuplicationConfig;
import com.almostreliable.unified.config.ServerConfigs;
import com.almostreliable.unified.config.UnifyConfig;
import com.almostreliable.unified.recipe.RecipeDumper;
import com.almostreliable.unified.recipe.RecipeTransformer;
import com.almostreliable.unified.recipe.unifier.RecipeHandlerFactory;
import com.almostreliable.unified.utils.TagMap;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.Map;
import java.util.Optional;

public final class AlmostUnifiedRuntimeImpl implements AlmostUnifiedRuntime {

    private final UnifyConfig unifyConfig;
    private final DuplicationConfig duplicationConfig;
    private final DebugConfig debugConfig;
    private final TagMap<Item> tagMap;
    private final ReplacementMap replacementMap;
    private final RecipeHandlerFactory recipeHandlerFactory;

    AlmostUnifiedRuntimeImpl(
            ServerConfigs configs,
            TagMap<Item> tagMap,
            ReplacementMap repMap,
            RecipeHandlerFactory recipeHandlerFactory
    ) {
        this.unifyConfig = configs.getUnifyConfig();
        this.duplicationConfig = configs.getDupConfig();
        this.debugConfig = configs.getDebugConfig();
        this.tagMap = tagMap;
        this.replacementMap = repMap;
        this.recipeHandlerFactory = recipeHandlerFactory;
    }

    @Override
    public void run(Map<ResourceLocation, JsonElement> recipes, boolean skipClientTracking) {
        debugConfig.logRecipes(recipes, "recipes_before_unification.txt");
        debugConfig.logUnifyTagDump(tagMap);

        long startTime = System.currentTimeMillis();
        RecipeTransformer.Result result = new RecipeTransformer(
                recipeHandlerFactory,
                replacementMap,
                unifyConfig,
                duplicationConfig
        ).transformRecipes(recipes, skipClientTracking);
        RecipeDumper dumper = new RecipeDumper(result, startTime, System.currentTimeMillis());
        dumper.dump(debugConfig.dumpOverview, debugConfig.dumpUnification, debugConfig.dumpDuplicates);

        debugConfig.logRecipes(recipes, "recipes_after_unification.txt");
    }

    @Override
    public Optional<TagMap<Item>> getFilteredTagMap() {
        return Optional.of(tagMap);
    }

    @Override
    public Optional<ReplacementMap> getReplacementMap() {
        return Optional.of(replacementMap);
    }

    @Override
    public Optional<UnifyConfig> getUnifyConfig() {
        return Optional.of(unifyConfig);
    }
}

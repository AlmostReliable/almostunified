package com.almostreliable.unified;

import com.almostreliable.unified.api.StoneStrataHandler;
import com.almostreliable.unified.config.DebugConfig;
import com.almostreliable.unified.config.DuplicationConfig;
import com.almostreliable.unified.config.ServerConfigs;
import com.almostreliable.unified.config.UnifyConfig;
import com.almostreliable.unified.recipe.RecipeDumper;
import com.almostreliable.unified.recipe.RecipeTransformer;
import com.almostreliable.unified.recipe.unifier.RecipeHandlerFactory;
import com.almostreliable.unified.utils.FileUtils;
import com.almostreliable.unified.utils.ReplacementMap;
import com.almostreliable.unified.utils.TagMap;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public final class AlmostUnifiedRuntimeImpl implements AlmostUnifiedRuntime {

    private final RecipeHandlerFactory recipeHandlerFactory;
    private final TagMap filteredTagMap;
    private final DuplicationConfig dupConfig;
    private final UnifyConfig unifyConfig;
    private final DebugConfig debugConfig;
    private final ReplacementMap replacementMap;

    private AlmostUnifiedRuntimeImpl(
            RecipeHandlerFactory recipeHandlerFactory,
            TagMap filteredTagMap,
            ReplacementMap replacementMap,
            DuplicationConfig dupConfig,
            UnifyConfig unifyConfig,
            DebugConfig debugConfig
    ) {
        this.recipeHandlerFactory = recipeHandlerFactory;
        this.filteredTagMap = filteredTagMap;
        this.replacementMap = replacementMap;
        this.dupConfig = dupConfig;
        this.unifyConfig = unifyConfig;
        this.debugConfig = debugConfig;
    }

    public static AlmostUnifiedRuntimeImpl create(TagManager tagManager, ServerConfigs serverConfigs) {
        createGitIgnoreIfNotExists();
        DuplicationConfig dupConfig = serverConfigs.getDupConfig();
        UnifyConfig unifyConfig = serverConfigs.getUnifyConfig();
        DebugConfig debugConfig = serverConfigs.getDebugConfig();

        RecipeHandlerFactory factory = new RecipeHandlerFactory();
        AlmostUnifiedPlatform.INSTANCE.bindRecipeHandlers(factory);

        var allowedTags = unifyConfig.bakeTags();
        TagMap globalTagMap = TagMap.create(tagManager);
        TagMap filteredTagMap = globalTagMap.filtered(allowedTags::contains, unifyConfig::includeItem);

        StoneStrataHandler stoneStrataHandler = StoneStrataHandler.create(unifyConfig.getStoneStrata(),
                AlmostUnifiedPlatform.INSTANCE.getStoneStrataTags(unifyConfig.getStoneStrata()), globalTagMap);

        var replacementMap = new ReplacementMap(filteredTagMap, stoneStrataHandler, unifyConfig);

        return new AlmostUnifiedRuntimeImpl(
                factory,
                filteredTagMap,
                replacementMap,
                dupConfig,
                unifyConfig,
                debugConfig
        );
    }

    private static void createGitIgnoreIfNotExists() {
        Path path = AlmostUnifiedPlatform.INSTANCE.getConfigPath();
        if (!(Files.exists(path) && Files.isDirectory(path))) {
            FileUtils.write(AlmostUnifiedPlatform.INSTANCE.getConfigPath(),
                    ".gitignore",
                    sb -> sb.append(DebugConfig.NAME).append(".json").append("\n"));
        }
    }

    @Override
    public void run(Map<ResourceLocation, JsonElement> recipes, boolean skipClientTracking) {
        debugConfig.logRecipes(recipes, "recipes_before_unification.txt");
        debugConfig.logUnifyTagDump(filteredTagMap);

        long startTime = System.currentTimeMillis();
        RecipeTransformer.Result result = new RecipeTransformer(recipeHandlerFactory,
                replacementMap,
                unifyConfig,
                dupConfig).transformRecipes(recipes, skipClientTracking);
        RecipeDumper dumper = new RecipeDumper(result, startTime, System.currentTimeMillis());
        dumper.dump(debugConfig.dumpOverview, debugConfig.dumpUnification, debugConfig.dumpDuplicates);

        debugConfig.logRecipes(recipes, "recipes_after_unification.txt");
    }

    @Override
    public Optional<TagMap> getFilteredTagMap() {
        return Optional.of(filteredTagMap);
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

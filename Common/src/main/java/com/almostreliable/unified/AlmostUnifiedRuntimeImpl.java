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
import com.almostreliable.unified.utils.TagOwnerships;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public final class AlmostUnifiedRuntimeImpl implements AlmostUnifiedRuntime {

    private final UnifyConfig unifyConfig;
    private final DuplicationConfig duplicationConfig;
    private final DebugConfig debugConfig;
    private final TagMap filteredTagMap;
    private final TagOwnerships tagOwnerships;
    private final ReplacementMap replacementMap;
    private final RecipeHandlerFactory recipeHandlerFactory;

    private AlmostUnifiedRuntimeImpl(
            UnifyConfig unifyConfig,
            DuplicationConfig duplicationConfig,
            DebugConfig debugConfig,
            TagMap filteredTagMap,
            TagOwnerships tagOwnerships,
            ReplacementMap replacementMap,
            RecipeHandlerFactory recipeHandlerFactory
    ) {
        this.unifyConfig = unifyConfig;
        this.duplicationConfig = duplicationConfig;
        this.debugConfig = debugConfig;
        this.filteredTagMap = filteredTagMap;
        this.tagOwnerships = tagOwnerships;
        this.replacementMap = replacementMap;
        this.recipeHandlerFactory = recipeHandlerFactory;
    }

    public static AlmostUnifiedRuntimeImpl create(ServerConfigs serverConfigs, TagManager tagManager, TagOwnerships tagOwnerships) {
        createGitIgnoreIfNotExists();

        UnifyConfig unifyConfig = serverConfigs.getUnifyConfig();
        DuplicationConfig duplicationConfig = serverConfigs.getDupConfig();
        DebugConfig debugConfig = serverConfigs.getDebugConfig();

        var unifyTags = unifyConfig.bakeTags();
        TagMap globalTagMap = TagMap.create(tagManager);
        TagMap filteredTagMap = globalTagMap.filtered(unifyTags::contains, unifyConfig::includeItem);

        StoneStrataHandler stoneStrataHandler = StoneStrataHandler.create(unifyConfig.getStoneStrata(),
                AlmostUnifiedPlatform.INSTANCE.getStoneStrataTags(unifyConfig.getStoneStrata()), globalTagMap);
        var replacementMap = new ReplacementMap(unifyConfig, filteredTagMap, stoneStrataHandler, tagOwnerships);

        RecipeHandlerFactory recipeHandlerFactory = new RecipeHandlerFactory();
        AlmostUnifiedPlatform.INSTANCE.bindRecipeHandlers(recipeHandlerFactory);

        return new AlmostUnifiedRuntimeImpl(
                unifyConfig,
                duplicationConfig,
                debugConfig,
                filteredTagMap,
                tagOwnerships,
                replacementMap,
                recipeHandlerFactory
        );
    }

    @Override
    public void run(Map<ResourceLocation, JsonElement> recipes, boolean skipClientTracking) {
        debugConfig.logRecipes(recipes, "recipes_before_unification.txt");
        debugConfig.logUnifyTagDump(filteredTagMap);

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

    private static void createGitIgnoreIfNotExists() {
        Path path = AlmostUnifiedPlatform.INSTANCE.getConfigPath();
        if (!(Files.exists(path) && Files.isDirectory(path))) {
            FileUtils.write(
                    AlmostUnifiedPlatform.INSTANCE.getConfigPath(),
                    ".gitignore",
                    sb -> sb.append(DebugConfig.NAME).append(".json").append("\n")
            );
        }
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

    @Override
    public Optional<TagOwnerships> getTagOwnerships() {
        return Optional.of(tagOwnerships);
    }
}

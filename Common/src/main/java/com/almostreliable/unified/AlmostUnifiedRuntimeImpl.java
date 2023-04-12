package com.almostreliable.unified;

import com.almostreliable.unified.api.StoneStrataHandler;
import com.almostreliable.unified.config.Config;
import com.almostreliable.unified.config.DebugConfig;
import com.almostreliable.unified.config.DuplicationConfig;
import com.almostreliable.unified.config.UnifyConfig;
import com.almostreliable.unified.recipe.RecipeDumper;
import com.almostreliable.unified.recipe.RecipeTransformer;
import com.almostreliable.unified.recipe.unifier.RecipeHandlerFactory;
import com.almostreliable.unified.utils.*;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagManager;
import net.minecraft.world.item.Item;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class AlmostUnifiedRuntimeImpl implements AlmostUnifiedRuntime {
    private final RecipeHandlerFactory recipeHandlerFactory;
    private final TagMap filteredTagMap;
    private final DuplicationConfig dupConfig;
    private final UnifyConfig unifyConfig;
    private final DebugConfig debugConfig;
    private final TagDelegateHelper tagDelegates;
    private final ReplacementMap replacementMap;

    private AlmostUnifiedRuntimeImpl(RecipeHandlerFactory recipeHandlerFactory, TagMap tagMap, DuplicationConfig dupConfig, UnifyConfig unifyConfig, DebugConfig debugConfig, TagDelegateHelper tagDelegates) {
        this.recipeHandlerFactory = recipeHandlerFactory;
        this.dupConfig = dupConfig;
        this.unifyConfig = unifyConfig;
        this.debugConfig = debugConfig;
        List<UnifyTag<Item>> allowedTags = unifyConfig.bakeTags();
        tagDelegates.validate(allowedTags);
        this.filteredTagMap = tagMap.filtered(allowedTags::contains, unifyConfig::includeItem);
        StoneStrataHandler stoneStrataHandler = StoneStrataHandler.create(unifyConfig.getStoneStrata(),
                AlmostUnifiedPlatform.INSTANCE.getStoneStrataTags(unifyConfig.getStoneStrata()),
                tagMap);
        this.replacementMap = new ReplacementMap(filteredTagMap, stoneStrataHandler, unifyConfig);
    }

    public static AlmostUnifiedRuntimeImpl create(TagManager tagManager) {
        createGitIgnoreIfNotExists();
        DuplicationConfig dupConfig = Config.load(DuplicationConfig.NAME, new DuplicationConfig.Serializer());
        UnifyConfig unifyConfig = Config.load(UnifyConfig.NAME, new UnifyConfig.Serializer());
        DebugConfig debugConfig = Config.load(DebugConfig.NAME, new DebugConfig.Serializer());

        RecipeHandlerFactory factory = new RecipeHandlerFactory();
        AlmostUnifiedPlatform.INSTANCE.bindRecipeHandlers(factory);

        TagDelegateHelper tagDelegates = new TagDelegateHelper(unifyConfig.getTagDelegates());
        TagMap tagMap = TagMap.create(tagManager, tagDelegates);
        return new AlmostUnifiedRuntimeImpl(factory, tagMap, dupConfig, unifyConfig, debugConfig, tagDelegates);
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

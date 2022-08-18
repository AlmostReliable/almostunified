package com.almostreliable.unified;

import com.almostreliable.unified.config.Config;
import com.almostreliable.unified.config.DebugConfig;
import com.almostreliable.unified.config.DuplicationConfig;
import com.almostreliable.unified.config.UnifyConfig;
import com.almostreliable.unified.recipe.RecipeDumper;
import com.almostreliable.unified.recipe.RecipeTransformer;
import com.almostreliable.unified.recipe.unifier.RecipeHandlerFactory;
import com.almostreliable.unified.utils.FileUtils;
import com.almostreliable.unified.utils.ReplacementMap;
import com.almostreliable.unified.utils.TagMap;
import com.almostreliable.unified.utils.UnifyTag;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagManager;
import net.minecraft.world.item.Item;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AlmostUnifiedRuntime {
    protected final RecipeHandlerFactory recipeHandlerFactory;
    private final TagMap tagMap;
    private final DuplicationConfig dupConfig;
    private final UnifyConfig unifyConfig;
    private final DebugConfig debugConfig;
    private final ReplacementMap replacementMap;

    private AlmostUnifiedRuntime(RecipeHandlerFactory recipeHandlerFactory, TagMap tagMap, DuplicationConfig dupConfig, UnifyConfig unifyConfig, DebugConfig debugConfig) {
        this.recipeHandlerFactory = recipeHandlerFactory;
        this.tagMap = tagMap;
        this.dupConfig = dupConfig;
        this.unifyConfig = unifyConfig;
        this.debugConfig = debugConfig;
        this.replacementMap = new ReplacementMap(this.tagMap, this.unifyConfig);
    }

    public static AlmostUnifiedRuntime create(TagManager tagManager) {
        Objects.requireNonNull(tagManager);

        createGitIgnoreIfNotExists();
        DuplicationConfig dupConfig = Config.load(DuplicationConfig.NAME, new DuplicationConfig.Serializer());
        UnifyConfig unifyConfig = Config.load(UnifyConfig.NAME, new UnifyConfig.Serializer());
        DebugConfig debugConfig = Config.load(DebugConfig.NAME, new DebugConfig.Serializer());

        RecipeHandlerFactory factory = new RecipeHandlerFactory();
        AlmostUnifiedPlatform.INSTANCE.bindRecipeHandlers(factory);

        List<UnifyTag<Item>> allowedTags = unifyConfig.bakeTags();
        TagMap tagMap = TagMap.create(tagManager, allowedTags::contains);

        return new AlmostUnifiedRuntime(factory, tagMap, dupConfig, unifyConfig, debugConfig);
    }

    public void run(Map<ResourceLocation, JsonElement> recipes) {
        debugConfig.logRecipes(recipes, "recipes_before_unification.txt");
        debugConfig.logUnifyTagDump(tagMap);

        long startTime = System.currentTimeMillis();
        RecipeTransformer.Result result = new RecipeTransformer(recipeHandlerFactory,
                replacementMap,
                unifyConfig,
                dupConfig).transformRecipes(recipes);
        RecipeDumper dumper = new RecipeDumper(result, startTime, System.currentTimeMillis());
        dumper.dump(debugConfig.dumpOverview, debugConfig.dumpUnification, debugConfig.dumpDuplicates);

        debugConfig.logRecipes(recipes, "recipes_after_unification.txt");
    }

    public TagMap getTagMap() {
        return tagMap;
    }

    public ReplacementMap getReplacementMap() {
        return replacementMap;
    }

    private static void createGitIgnoreIfNotExists() {
        Path path = AlmostUnifiedPlatform.INSTANCE.getConfigPath();
        if (!(Files.exists(path) && Files.isDirectory(path))) {
            FileUtils.write(AlmostUnifiedPlatform.INSTANCE.getConfigPath(),
                    ".gitignore",
                    sb -> sb.append(DebugConfig.NAME).append(".json").append("\n"));
        }
    }
}

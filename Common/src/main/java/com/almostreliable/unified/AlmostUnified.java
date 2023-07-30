package com.almostreliable.unified;

import com.almostreliable.unified.api.StoneStrataHandler;
import com.almostreliable.unified.config.Config;
import com.almostreliable.unified.config.ServerConfigs;
import com.almostreliable.unified.config.StartupConfig;
import com.almostreliable.unified.config.UnifyConfig;
import com.almostreliable.unified.recipe.unifier.RecipeHandlerFactory;
import com.almostreliable.unified.utils.ReplacementMap;
import com.almostreliable.unified.utils.TagMap;
import com.almostreliable.unified.utils.TagOwnerships;
import com.almostreliable.unified.utils.TagReloadHandler;
import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagManager;
import net.minecraft.world.item.Item;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
public final class AlmostUnified {

    public static final Logger LOG = LogManager.getLogger(BuildConfig.MOD_NAME);

    @Nullable private static AlmostUnifiedRuntime RUNTIME;
    @Nullable private static TagManager TAG_MANAGER;
    @Nullable private static StartupConfig STARTUP_CONFIG;

    public static boolean isRuntimeLoaded() {
        return RUNTIME != null;
    }

    public static AlmostUnifiedRuntime getRuntime() {
        if (RUNTIME == null) {
            return AlmostUnifiedFallbackRuntime.getInstance();
        }
        return RUNTIME;
    }

    public static StartupConfig getStartupConfig() {
        if (STARTUP_CONFIG == null) {
            STARTUP_CONFIG = Config.load(StartupConfig.NAME, new StartupConfig.Serializer());
        }
        return STARTUP_CONFIG;
    }

    public static void onTagManagerReload(TagManager tagManager) {
        TAG_MANAGER = tagManager;
    }

    public static void onTagLoaderReload(Map<ResourceLocation, Collection<Holder<Item>>> tags) {
        Preconditions.checkNotNull(TAG_MANAGER, "TagManager was not loaded correctly");

        ServerConfigs serverConfigs = ServerConfigs.load();
        UnifyConfig unifyConfig = serverConfigs.getUnifyConfig();

        var unifyTags = unifyConfig.bakeTags();

        TagOwnerships tagOwnerships = new TagOwnerships(unifyTags, unifyConfig.getTagOwnerships());
        tagOwnerships.applyOwnerships(tags);

        TagMap globalTagMap = TagMap.create(tags);
        TagMap filteredTagMap = globalTagMap.filtered(unifyTags::contains, unifyConfig::includeItem);

        StoneStrataHandler stoneStrataHandler = StoneStrataHandler.create(
                unifyConfig.getStoneStrata(),
                AlmostUnifiedPlatform.INSTANCE.getStoneStrataTags(unifyConfig.getStoneStrata()),
                globalTagMap
        );

        ReplacementMap repMap = new ReplacementMap(unifyConfig, filteredTagMap, stoneStrataHandler, tagOwnerships);

        TagReloadHandler.applyInheritance(unifyConfig, globalTagMap, filteredTagMap, repMap);

        RecipeHandlerFactory recipeHandlerFactory = new RecipeHandlerFactory();
        AlmostUnifiedPlatform.INSTANCE.bindRecipeHandlers(recipeHandlerFactory);

        RUNTIME = new AlmostUnifiedRuntimeImpl(serverConfigs, filteredTagMap, repMap, recipeHandlerFactory);
    }

    public static void onRecipeManagerReload(Map<ResourceLocation, JsonElement> recipes) {
        Preconditions.checkNotNull(RUNTIME, "AlmostUnifiedRuntime was not loaded correctly");
        RUNTIME.run(recipes, getStartupConfig().isServerOnly());
    }
}

package com.almostreliable.unified;

import com.almostreliable.unified.config.Config;
import com.almostreliable.unified.config.ServerConfigs;
import com.almostreliable.unified.config.StartupConfig;
import com.almostreliable.unified.config.UnifyConfig;
import com.almostreliable.unified.recipe.unifier.RecipeHandlerFactory;
import com.almostreliable.unified.utils.TagOwnerships;
import com.almostreliable.unified.utils.TagReloadHandler;
import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
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

    public static void onTagLoaderReload(Map<ResourceLocation, Collection<Holder<Item>>> tags) {
        RecipeHandlerFactory recipeHandlerFactory = new RecipeHandlerFactory();
        AlmostUnifiedPlatform.INSTANCE.bindRecipeHandlers(recipeHandlerFactory);

        ServerConfigs serverConfigs = ServerConfigs.load();
        UnifyConfig unifyConfig = serverConfigs.getUnifyConfig();

        TagOwnerships tagOwnerships = new TagOwnerships(unifyConfig.bakeAndValidateTags(tags),
                unifyConfig.getTagOwnerships());
        tagOwnerships.applyOwnerships(tags);

        ReplacingData replacingData = loadWithTagInheritance(tags, unifyConfig, tagOwnerships);

        RUNTIME = new AlmostUnifiedRuntimeImpl(serverConfigs,
                replacingData.filteredTagMap(),
                replacingData.replacementMap(),
                recipeHandlerFactory);
    }

    /**
     * Loads base data for replacing and also applies tag inheritance if configured. As tag inheritance will potentially
     * mutate <b>{@code tags}</b> we have to reload the data with the new <b>{@code tags}</b>.
     *
     * @param tags          The vanilla tag map provided by the TagManager
     * @param unifyConfig   The mod config to use for unifying
     * @param tagOwnerships The tag ownerships to apply
     * @return The loaded data
     */
    private static ReplacingData loadWithTagInheritance(Map<ResourceLocation, Collection<Holder<Item>>> tags, UnifyConfig unifyConfig, TagOwnerships tagOwnerships) {
        ReplacingData replacingData = ReplacingData.load(tags, unifyConfig, tagOwnerships);
        var needsRebuild = TagReloadHandler.applyInheritance(unifyConfig, replacingData);
        if (needsRebuild) {
            return ReplacingData.load(tags, unifyConfig, tagOwnerships);
        }

        return replacingData;
    }


    public static void onRecipeManagerReload(Map<ResourceLocation, JsonElement> recipes) {
        Preconditions.checkNotNull(RUNTIME, "AlmostUnifiedRuntime was not loaded correctly");
        RUNTIME.run(recipes, getStartupConfig().isServerOnly());
    }
}

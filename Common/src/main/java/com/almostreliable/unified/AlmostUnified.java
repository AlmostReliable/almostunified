package com.almostreliable.unified;

import com.almostreliable.unified.api.TagInheritance;
import com.almostreliable.unified.api.TagOwnerships;
import com.almostreliable.unified.api.UnifierRegistry;
import com.almostreliable.unified.api.UnifySettings;
import com.almostreliable.unified.config.*;
import com.almostreliable.unified.recipe.unifier.UnifierRegistryImpl;
import com.almostreliable.unified.utils.TagOwnershipsImpl;
import com.almostreliable.unified.utils.TagReloadHandler;
import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
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
        UnifierRegistry unifierRegistry = new UnifierRegistryImpl();
        PluginManager.instance().registerUnifiers(unifierRegistry);

        ServerConfigs serverConfigs = ServerConfigs.load();
        TagConfig tagConfig = serverConfigs.getTagConfig();
        ReplacementsConfig replacementsConfig = serverConfigs.getReplacementsConfig();
        DuplicationConfig dupConfig = serverConfigs.getDupConfig();
        DebugConfig debugConfig = serverConfigs.getDebugConfig();

        UnifySettings unifySettings = serverConfigs.getUnifyConfig().bake(tags, replacementsConfig);

        TagReloadHandler.applyCustomTags(tagConfig.getCustomTags());

        TagOwnershipsImpl tagOwnerships = new TagOwnershipsImpl(
                unifySettings.getTags(),
                tagConfig.getTagOwnerships()
        );
        tagOwnerships.applyOwnerships(tags);

        ReplacementData replacementData = loadReplacementData(tags,
                unifySettings,
                tagConfig.getItemTagInheritance(),
                tagConfig.getBlockTagInheritance(),
                tagOwnerships);

        RUNTIME = new AlmostUnifiedRuntimeImpl(
                unifySettings,
                dupConfig,
                debugConfig,
                replacementData.filteredTagMap(),
                replacementData.replacementMap(),
                unifierRegistry
        );
    }

    public static void onRecipeManagerReload(Map<ResourceLocation, JsonElement> recipes) {
        Preconditions.checkNotNull(RUNTIME, "AlmostUnifiedRuntime was not loaded correctly");
        RUNTIME.run(recipes, getStartupConfig().isServerOnly());
    }

    /**
     * Loads the required data for the replacement logic.
     * <p>
     * This method applies tag inheritance and rebuilds the replacement data if the
     * inheritance mutates the tags.
     *
     * @param tags                The vanilla tag map provided by the TagManager
     * @param unifySettings       The unify settings
     * @param itemTagInheritance  The item tag inheritance
     * @param blockTagInheritance The block tag inheritance
     * @param tagOwnerships       The tag ownerships to apply
     * @return The loaded data
     */
    private static ReplacementData loadReplacementData(Map<ResourceLocation, Collection<Holder<Item>>> tags, UnifySettings unifySettings, TagInheritance<Item> itemTagInheritance, TagInheritance<Block> blockTagInheritance, TagOwnerships tagOwnerships) {
        ReplacementData replacementData = ReplacementData.load(tags, unifySettings, tagOwnerships);
        var needsRebuild = TagReloadHandler.applyInheritance(itemTagInheritance, blockTagInheritance, replacementData);
        if (needsRebuild) {
            return ReplacementData.load(tags, unifySettings, tagOwnerships);
        }

        return replacementData;
    }
}

package com.almostreliable.unified;

import com.almostreliable.unified.api.*;
import com.almostreliable.unified.config.*;
import com.almostreliable.unified.impl.AlmostUnifiedRuntimeImpl;
import com.almostreliable.unified.impl.TagInheritance;
import com.almostreliable.unified.impl.TagOwnershipsImpl;
import com.almostreliable.unified.impl.UnifyHandlerImpl;
import com.almostreliable.unified.loot.LootUnification;
import com.almostreliable.unified.recipe.RecipeUnifyHandler;
import com.almostreliable.unified.recipe.unifier.UnifierRegistryImpl;
import com.almostreliable.unified.utils.FileUtils;
import com.almostreliable.unified.utils.TagReloadHandler;
import com.almostreliable.unified.utils.VanillaTagWrapper;
import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
public final class AlmostUnified {

    private static final LoggerFactory.Policy LOGGER_POLICY = new LoggerFactory.Policy();
    public static final Logger LOG = LoggerFactory.createCustomLogger(LOGGER_POLICY);

    @Nullable private static AlmostUnifiedRuntime RUNTIME;
    @Nullable private static StartupConfig STARTUP_CONFIG;

    public static boolean isRuntimeLoaded() {
        return RUNTIME != null;
    }

    @Nullable
    static AlmostUnifiedRuntime getRuntime() {
        return RUNTIME;
    }

    public static StartupConfig getStartupConfig() {
        if (STARTUP_CONFIG == null) {
            STARTUP_CONFIG = Config.load(StartupConfig.NAME, new StartupConfig.Serializer());
        }
        return STARTUP_CONFIG;
    }

    public static void onTagLoaderReload(VanillaTagWrapper<Item> itemTags, VanillaTagWrapper<Block> blockTags) {
        LOGGER_POLICY.reset();
        UnifierRegistry unifierRegistry = new UnifierRegistryImpl();
        PluginManager.instance().registerUnifiers(unifierRegistry);

        FileUtils.createGitIgnoreIfNotExists();
        TagConfig tagConfig = Config.load(TagConfig.NAME, new TagConfig.Serializer());
        PlaceholdersConfig replacementsConfig = Config.load(PlaceholdersConfig.NAME,
                new PlaceholdersConfig.Serializer());
        DuplicationConfig dupConfig = Config.load(DuplicationConfig.NAME, new DuplicationConfig.Serializer());
        DebugConfig debugConfig = Config.load(DebugConfig.NAME, new DebugConfig.Serializer());

        Collection<UnifyConfig> unifyConfigs = UnifyConfig.safeLoadConfigs();
        logMissingPriorityMods(unifyConfigs);
        Set<TagKey<Item>> allUnifyTags = bakeAndValidateTags(unifyConfigs, itemTags, replacementsConfig);

        TagReloadHandler.applyCustomTags(tagConfig.getCustomTags(), itemTags);
        TagOwnershipsImpl tagOwnerships = new TagOwnershipsImpl(allUnifyTags::contains, tagConfig.getTagOwnerships());
        tagOwnerships.applyOwnerships(itemTags);

        List<UnifyHandler> unifyHandlers = createAndPrepareUnifyHandlers(itemTags,
                blockTags,
                unifyConfigs,
                tagOwnerships,
                tagConfig.getTagInheritance());
        ItemHider.applyHideTags(itemTags, unifyHandlers);

        RUNTIME = new AlmostUnifiedRuntimeImpl(unifyHandlers,
                dupConfig,
                debugConfig,
                unifierRegistry,
                tagOwnerships,
                replacementsConfig);
    }

    private static void logMissingPriorityMods(Collection<UnifyConfig> unifyConfigs) {
        Set<String> mods = unifyConfigs
                .stream()
                .map(UnifyConfig::getModPriorities)
                .flatMap(ModPriorities::stream)
                .collect(Collectors.toSet());

        for (String mod : mods) {
            if (!AlmostUnifiedPlatform.INSTANCE.isModLoaded(mod)) {
                LOG.warn("Mod '{}' is not loaded, but used in unify settings", mod);
            }
        }
    }


    /**
     * Creates all unify handlers for further unification.
     * <p>
     * This method also applies tag inheritance. If tag inheritance was applied, all handlers will be rebuilt due to tag inheritance modifications against vanilla tags.
     *
     * @param itemTags       All existing item tags which are used ingame
     * @param blockTags      All existing block tags which are used ingame
     * @param unifyConfigs   All unify configs
     * @param tagOwnerships  All tag ownerships
     * @param tagInheritance Tag inheritance data
     * @return All unify handlers
     */
    private static List<UnifyHandler> createAndPrepareUnifyHandlers(VanillaTagWrapper<Item> itemTags, VanillaTagWrapper<Block> blockTags, Collection<UnifyConfig> unifyConfigs, TagOwnershipsImpl tagOwnerships, TagInheritance tagInheritance) {
        List<UnifyHandler> unifyHandlers = UnifyHandlerImpl.create(unifyConfigs, itemTags, tagOwnerships);
        var needsRebuild = tagInheritance.apply(itemTags, blockTags, unifyHandlers);
        if (needsRebuild) {
            unifyHandlers = UnifyHandlerImpl.create(unifyConfigs, itemTags, tagOwnerships);
        }

        return unifyHandlers;
    }

    /**
     * Bake all tags from unify configs and validate them.
     * Validating contains:
     * <ul>
     * <li>Tag must exist in vanilla tags, which means that the tag is in used by either vanilla or any mods.</li>
     * <li>Tag must not exist in another unify config. If found, the tag will be skipped.</li>
     * </ul>
     *
     * @param unifyConfigs The unify configs
     * @param itemTags     The vanilla tags
     * @param placeholders The replacements
     * @return The baked tags combined from all unify configs
     */
    private static Set<TagKey<Item>> bakeAndValidateTags(Collection<UnifyConfig> unifyConfigs, VanillaTagWrapper<Item> itemTags, Placeholders placeholders) {
        Set<TagKey<Item>> result = new HashSet<>();

        Map<TagKey<Item>, String> visitedTags = new HashMap<>();
        Set<TagKey<Item>> wrongTags = new HashSet<>();

        for (UnifyConfig config : unifyConfigs) {
            Predicate<TagKey<Item>> validator = tag -> {
                if (!itemTags.has(tag)) {
                    wrongTags.add(tag);
                    return false;
                }

                if (visitedTags.containsKey(tag)) {
                    AlmostUnified.LOG.warn("Tag '{}' from unify config '{}' was already created in unify config '{}'",
                            config.getName(),
                            tag.location(),
                            visitedTags.get(tag));
                    return false;
                }

                visitedTags.put(tag, config.getName());
                return true;
            };

            Set<TagKey<Item>> tags = config.bakeTags(validator, placeholders);
            result.addAll(tags);
        }

        if (!wrongTags.isEmpty()) {
            AlmostUnified.LOG.warn("The following tags are invalid or not in use and will be ignored: {}",
                    wrongTags.stream().map(TagKey::location).collect(Collectors.toList()));
        }

        return result;
    }

    public static void onRecipeManagerReload(Map<ResourceLocation, JsonElement> recipes, HolderLookup.Provider registries) {
        Preconditions.checkNotNull(RUNTIME, "AlmostUnifiedRuntime was not loaded correctly");
        if (RUNTIME instanceof RecipeUnifyHandler handler) {
            handler.run(recipes, getStartupConfig().isServerOnly());
        } else {
            AlmostUnified.LOG.error(
                    "Internal error. Implementation of given AlmostUnifiedRuntime does not implement RecipeUnifyHandler!");
        }

        LootUnification.unifyLoot(RUNTIME, registries);
    }

    private static Layout<? extends Serializable> getLayout(@Nullable Appender appender, Configuration config) {
        if (appender == null) {
            return PatternLayout
                    .newBuilder()
                    .withConfiguration(config)
                    .withPattern("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %msg%n")
                    .build();
        }

        return appender.getLayout();
    }
}

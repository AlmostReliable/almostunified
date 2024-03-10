package com.almostreliable.unified;

import com.almostreliable.unified.api.Replacements;
import com.almostreliable.unified.api.UnifierRegistry;
import com.almostreliable.unified.api.UnifyHandler;
import com.almostreliable.unified.config.*;
import com.almostreliable.unified.impl.TagMapImpl;
import com.almostreliable.unified.impl.TagOwnershipsImpl;
import com.almostreliable.unified.impl.UnifyHandlerImpl;
import com.almostreliable.unified.recipe.unifier.UnifierRegistryImpl;
import com.almostreliable.unified.utils.FileUtils;
import com.almostreliable.unified.utils.TagReloadHandler;
import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
            return new EmptyAlmostUnifiedRuntime();
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

        FileUtils.createGitIgnoreIfNotExists();
        TagConfig tagConfig = Config.load(TagConfig.NAME, new TagConfig.Serializer());
        ReplacementsConfig replacementsConfig = Config.load(ReplacementsConfig.NAME,
                new ReplacementsConfig.Serializer());
        DuplicationConfig dupConfig = Config.load(DuplicationConfig.NAME, new DuplicationConfig.Serializer());
        DebugConfig debugConfig = Config.load(DebugConfig.NAME, new DebugConfig.Serializer());

        Collection<UnifyConfig> unifyConfigs = UnifyConfig.safeLoadConfigs();
        Set<TagKey<Item>> allUnifyTags = allUnifyTags(unifyConfigs, tags, replacementsConfig);

        TagReloadHandler.applyCustomTags(tagConfig.getCustomTags());
        TagOwnershipsImpl tagOwnerships = new TagOwnershipsImpl(allUnifyTags::contains, tagConfig.getTagOwnerships());
        tagOwnerships.applyOwnerships(tags);

        List<UnifyHandler> unifyHandlers = createAndPrepareUnifySettings(tags, unifyConfigs, tagOwnerships, tagConfig);
        ItemHider.applyHideTags(tags, unifyHandlers);

        RUNTIME = new AlmostUnifiedRuntimeImpl(unifyHandlers, dupConfig, debugConfig, unifierRegistry);
    }

    private static List<UnifyHandler> createAndPrepareUnifySettings(Map<ResourceLocation, Collection<Holder<Item>>> tags, Collection<UnifyConfig> unifyConfigs, TagOwnershipsImpl tagOwnerships, TagConfig tagConfig) {
        var globalTagMap = TagMapImpl.createFromItemTags(tags);
        List<UnifyHandler> unifyHandlers = UnifyHandlerImpl.create(unifyConfigs, globalTagMap, tagOwnerships);
        var needsRebuild = TagReloadHandler.applyInheritance(tagConfig.getItemTagInheritance(),
                tagConfig.getBlockTagInheritance(),
                globalTagMap,
                unifyHandlers);
        if (needsRebuild) {
            unifyHandlers = UnifyHandlerImpl.create(unifyConfigs, TagMapImpl.createFromItemTags(tags), tagOwnerships);
        }

        return unifyHandlers;
    }

    private static Set<TagKey<Item>> allUnifyTags(Collection<UnifyConfig> unifyConfigs, Map<ResourceLocation, Collection<Holder<Item>>> vanillaTags, Replacements replacements) {
        Set<TagKey<Item>> result = new HashSet<>();

        Map<TagKey<Item>, String> visitedTags = new HashMap<>();
        Set<TagKey<Item>> wrongTags = new HashSet<>();

        for (UnifyConfig config : unifyConfigs) {
            Predicate<TagKey<Item>> validator = tag -> {
                if (!vanillaTags.containsKey(tag.location())) {
                    wrongTags.add(tag);
                    return false;
                }

                if (visitedTags.containsKey(tag)) {
                    AlmostUnified.LOG.warn("Baked tag '{}' was already created in unify config '{}'",
                            tag.location(),
                            visitedTags.get(tag));
                    return false;
                }

                visitedTags.put(tag, config.getName());
                return false;
            };

            result.addAll(config.bakeTags(validator, replacements));
        }

        if (!wrongTags.isEmpty()) {
            AlmostUnified.LOG.warn("The following tags are invalid or not in use and will be ignored: {}",
                    wrongTags.stream().map(TagKey::location).collect(Collectors.toList()));
        }

        return result;
    }

    public static void onRecipeManagerReload(Map<ResourceLocation, JsonElement> recipes) {
        Preconditions.checkNotNull(RUNTIME, "AlmostUnifiedRuntime was not loaded correctly");
        RUNTIME.run(recipes, getStartupConfig().isServerOnly());
    }
}

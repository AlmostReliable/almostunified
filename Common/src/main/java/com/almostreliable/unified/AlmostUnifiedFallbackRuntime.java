package com.almostreliable.unified;

import com.almostreliable.unified.api.StoneStrataHandler;
import com.almostreliable.unified.config.Config;
import com.almostreliable.unified.config.UnifyConfig;
import com.almostreliable.unified.utils.ReplacementMap;
import com.almostreliable.unified.utils.TagMap;
import com.almostreliable.unified.utils.TagOwnerships;
import com.almostreliable.unified.utils.UnifyTag;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

// TODO: Implement sync so it's not just a fallback
public class AlmostUnifiedFallbackRuntime implements AlmostUnifiedRuntime {

    @Nullable private static AlmostUnifiedFallbackRuntime INSTANCE;
    @Nullable private UnifyConfig config;
    @Nullable private TagMap filteredTagMap;
    @Nullable private ReplacementMap replacementMap;
    @Nullable private TagOwnerships tagOwnerships;

    public static AlmostUnifiedFallbackRuntime getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AlmostUnifiedFallbackRuntime();
            INSTANCE.reload();
        }

        return INSTANCE;
    }

    public void reload() {
        config = null;
        filteredTagMap = null;
        replacementMap = null;
        tagOwnerships = null;
        build();
    }

    private UnifyConfig getConfig() {
        if (config == null) {
            config = Config.load(UnifyConfig.NAME, new UnifyConfig.Serializer());
        }

        return config;
    }

    public void build() {
        var unifyConfig = getConfig();
        Set<UnifyTag<Item>> unifyTags = unifyConfig.bakeTags();
        tagOwnerships = new TagOwnerships(unifyTags, unifyConfig.getTagOwnerships());
        filteredTagMap = TagMap.create(unifyTags).filtered($ -> true, unifyConfig::includeItem);
        StoneStrataHandler stoneStrataHandler = getStoneStrataHandler(unifyConfig);
        replacementMap = new ReplacementMap(Objects.requireNonNull(filteredTagMap), stoneStrataHandler, unifyConfig);
    }

    private static StoneStrataHandler getStoneStrataHandler(UnifyConfig config) {
        Set<UnifyTag<Item>> stoneStrataTags = AlmostUnifiedPlatform.INSTANCE.getStoneStrataTags(config.getStoneStrata());
        TagMap stoneStrataTagMap = TagMap.create(stoneStrataTags);
        return StoneStrataHandler.create(config.getStoneStrata(), stoneStrataTags, stoneStrataTagMap);
    }

    @Override
    public void run(Map<ResourceLocation, JsonElement> recipes, boolean skipClientTracking) {
        // no-op
    }

    @Override
    public Optional<TagMap> getFilteredTagMap() {
        return Optional.ofNullable(filteredTagMap);
    }

    @Override
    public Optional<ReplacementMap> getReplacementMap() {
        return Optional.ofNullable(replacementMap);
    }

    @Override
    public Optional<UnifyConfig> getUnifyConfig() {
        return Optional.ofNullable(config);
    }

    @Override
    public Optional<TagOwnerships> getTagDelegateHelper() {
        return Optional.ofNullable(tagOwnerships);
    }
}

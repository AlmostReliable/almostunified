package com.almostreliable.unified;

import com.almostreliable.unified.api.*;
import com.almostreliable.unified.config.Config;
import com.almostreliable.unified.config.ReplacementsConfig;
import com.almostreliable.unified.config.TagConfig;
import com.almostreliable.unified.config.UnifyConfig;
import com.almostreliable.unified.utils.ReplacementMapImpl;
import com.almostreliable.unified.utils.TagMapImpl;
import com.almostreliable.unified.utils.TagOwnershipsImpl;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

// TODO: Implement sync, so it's not just a fallback
public class AlmostUnifiedFallbackRuntime implements AlmostUnifiedRuntime {

    @Nullable private static AlmostUnifiedFallbackRuntime INSTANCE;

    @Nullable private UnifyConfig unifyConfig;
    @Nullable private TagMap<Item> filteredTagMap;
    @Nullable private ReplacementMap replacementMap;

    public static AlmostUnifiedFallbackRuntime getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AlmostUnifiedFallbackRuntime();
            INSTANCE.reload();
        }

        return INSTANCE;
    }

    public void reload() {
        unifyConfig = null;
        filteredTagMap = null;
        replacementMap = null;
        build();
    }

    private static StoneStrataLookup createStoneStrataLookup(UnifyConfig config) {
        Set<TagKey<Item>> stoneStrataTags = AlmostUnifiedPlatform.INSTANCE.getStoneStrataTags(config.getStoneStrata());
        TagMap<Item> stoneStrataTagMap = TagMapImpl.create(stoneStrataTags);
        return StoneStrataLookupImpl.create(config.getStoneStrata(), stoneStrataTags, stoneStrataTagMap);
    }

    private void build() {
        unifyConfig = Config.load(UnifyConfig.NAME, new UnifyConfig.Serializer());
        var tagConfig = Config.load(TagConfig.NAME, new TagConfig.Serializer());
        var replacementsConfig = Config.load(ReplacementsConfig.NAME, new ReplacementsConfig.Serializer());
        Set<TagKey<Item>> unifyTags = unifyConfig.bakeTags(replacementsConfig);
        filteredTagMap = TagMapImpl.create(unifyTags).filtered($ -> true, unifyConfig::includeItem);
        StoneStrataLookup stoneStrataLookup = createStoneStrataLookup(unifyConfig);
        TagOwnerships tagOwnerships = new TagOwnershipsImpl(unifyTags, tagConfig.getTagOwnerships());
        replacementMap = new ReplacementMapImpl(unifyConfig.getModPriorities(),
                filteredTagMap,
                stoneStrataLookup,
                tagOwnerships);
    }

    @Override
    public void run(Map<ResourceLocation, JsonElement> recipes, boolean skipClientTracking) {
        // no-op
    }

    @Override
    public Optional<TagMap<Item>> getFilteredTagMap() {
        return Optional.ofNullable(filteredTagMap);
    }

    @Override
    public Optional<ReplacementMap> getReplacementMap() {
        return Optional.ofNullable(replacementMap);
    }

    @Override
    public Optional<UnifyConfig> getUnifyConfig() {
        return Optional.ofNullable(unifyConfig);
    }
}

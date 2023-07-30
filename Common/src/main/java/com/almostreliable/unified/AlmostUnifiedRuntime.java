package com.almostreliable.unified;

import com.almostreliable.unified.config.UnifyConfig;
import com.almostreliable.unified.utils.ReplacementMap;
import com.almostreliable.unified.utils.TagMap;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.Map;
import java.util.Optional;

public interface AlmostUnifiedRuntime {

    void run(Map<ResourceLocation, JsonElement> recipes, boolean skipClientTracking);

    Optional<TagMap<Item>> getFilteredTagMap();

    Optional<ReplacementMap> getReplacementMap();

    Optional<UnifyConfig> getUnifyConfig();
}

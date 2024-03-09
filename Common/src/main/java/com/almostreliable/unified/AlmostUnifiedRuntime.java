package com.almostreliable.unified;

import com.almostreliable.unified.api.ReplacementMap;
import com.almostreliable.unified.api.TagMap;
import com.almostreliable.unified.api.UnifySettings;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.Map;

public interface AlmostUnifiedRuntime {

    void run(Map<ResourceLocation, JsonElement> recipes, boolean skipClientTracking);

    TagMap<Item> getFilteredTagMap();

    ReplacementMap getReplacementMap();

    UnifySettings getUnifyConfig();
}

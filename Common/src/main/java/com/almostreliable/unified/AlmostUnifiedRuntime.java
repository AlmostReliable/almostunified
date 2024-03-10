package com.almostreliable.unified;

import com.almostreliable.unified.api.ReplacementMap;
import com.almostreliable.unified.api.TagMap;
import com.almostreliable.unified.api.TagOwnerships;
import com.almostreliable.unified.api.UnifyHandler;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

public interface AlmostUnifiedRuntime {

    void run(Map<ResourceLocation, JsonElement> recipes, boolean skipClientTracking);

    TagMap<Item> getTagMap();

    ReplacementMap getReplacementMap();

    Collection<? extends UnifyHandler> getUnifyHandlers();

    @Nullable
    UnifyHandler getUnifyHandler(String name);

    TagOwnerships getTagOwnerships();
}

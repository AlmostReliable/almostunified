package com.almostreliable.unified;

import com.almostreliable.unified.api.TagOwnerships;
import com.almostreliable.unified.api.UnifyHandler;
import com.almostreliable.unified.api.UnifyLookup;
import com.almostreliable.unified.impl.CompositeUnifyLookup;
import com.almostreliable.unified.impl.TagOwnershipsImpl;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmptyAlmostUnifiedRuntime implements AlmostUnifiedRuntime {

    @Override
    public void run(Map<ResourceLocation, JsonElement> recipes, boolean skipClientTracking) {
        // no-op
    }

    @Override
    public UnifyLookup getUnifyLookup() {
        return new CompositeUnifyLookup(List.of(), getTagOwnerships());
    }

    @Override
    public Collection<? extends UnifyHandler> getUnifyHandlers() {
        return List.of();
    }

    @Nullable
    @Override
    public UnifyHandler getUnifyHandler(String name) {
        return null;
    }

    @Override
    public TagOwnerships getTagOwnerships() {
        return new TagOwnershipsImpl($ -> true, new HashMap<>());
    }
}

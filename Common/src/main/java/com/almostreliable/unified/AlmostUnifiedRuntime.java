package com.almostreliable.unified;

import com.almostreliable.unified.config.UnifyConfig;
import com.almostreliable.unified.utils.ReplacementMap;
import com.almostreliable.unified.utils.TagMap;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;

public interface AlmostUnifiedRuntime {

    AlmostUnifiedRuntime EMPTY = new AlmostUnifiedRuntime() {
        @Override
        public void run(Map<ResourceLocation, JsonElement> recipes, boolean skipClientTracking) {
            // no-op
        }

        @Override
        public Optional<TagMap> getFilteredTagMap() {
            return Optional.empty();
        }

        @Override
        public Optional<ReplacementMap> getReplacementMap() {
            return Optional.empty();
        }

        @Override
        public Optional<UnifyConfig> getUnifyConfig() {
            return Optional.empty();
        }
    };

    void run(Map<ResourceLocation, JsonElement> recipes, boolean skipClientTracking);

    Optional<TagMap> getFilteredTagMap();

    Optional<ReplacementMap> getReplacementMap();

    Optional<UnifyConfig> getUnifyConfig();
}

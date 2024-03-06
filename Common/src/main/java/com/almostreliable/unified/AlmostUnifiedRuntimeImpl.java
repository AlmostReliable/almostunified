package com.almostreliable.unified;

import com.almostreliable.unified.api.ReplacementMap;
import com.almostreliable.unified.api.TagMap;
import com.almostreliable.unified.api.UnifierRegistry;
import com.almostreliable.unified.api.UnifySettings;
import com.almostreliable.unified.config.DebugConfig;
import com.almostreliable.unified.config.DuplicationConfig;
import com.almostreliable.unified.config.UnifyConfig;
import com.almostreliable.unified.recipe.RecipeDumper;
import com.almostreliable.unified.recipe.RecipeTransformer;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.Map;
import java.util.Optional;

public final class AlmostUnifiedRuntimeImpl implements AlmostUnifiedRuntime {

    private final UnifySettings unifySettings;
    private final DuplicationConfig duplicationConfig;
    private final DebugConfig debugConfig;
    private final TagMap<Item> tagMap;
    private final ReplacementMap replacementMap;
    private final UnifierRegistry unifierRegistry;

    AlmostUnifiedRuntimeImpl(
            UnifySettings unifySettings,
            DuplicationConfig duplicationConfig,
            DebugConfig debugConfig,
            TagMap<Item> tagMap,
            ReplacementMap repMap,
            UnifierRegistry unifierRegistry
    ) {
        this.unifySettings = unifySettings;
        this.duplicationConfig = duplicationConfig;
        this.debugConfig = debugConfig;
        this.tagMap = tagMap;
        this.replacementMap = repMap;
        this.unifierRegistry = unifierRegistry;
    }

    @Override
    public void run(Map<ResourceLocation, JsonElement> recipes, boolean skipClientTracking) {
        debugConfig.logRecipes(recipes, "recipes_before_unification.txt");
        debugConfig.logUnifyTagDump(tagMap);

        long startTime = System.currentTimeMillis();
        RecipeTransformer.Result result = new RecipeTransformer(
                unifierRegistry,
                replacementMap,
                unifySettings,
                duplicationConfig
        ).transformRecipes(recipes, skipClientTracking);
        RecipeDumper dumper = new RecipeDumper(result, startTime, System.currentTimeMillis());
        dumper.dump(debugConfig.dumpOverview, debugConfig.dumpUnification, debugConfig.dumpDuplicates);

        debugConfig.logRecipes(recipes, "recipes_after_unification.txt");
    }

    @Override
    public Optional<TagMap<Item>> getFilteredTagMap() {
        return Optional.of(tagMap);
    }

    @Override
    public Optional<ReplacementMap> getReplacementMap() {
        return Optional.of(replacementMap);
    }

    @Override
    public Optional<UnifyConfig> getUnifyConfig() {
        return Optional.empty();
    }
}

package com.almostreliable.unified;

import com.almostreliable.unified.api.*;
import com.almostreliable.unified.config.DebugConfig;
import com.almostreliable.unified.config.DuplicationConfig;
import com.almostreliable.unified.recipe.RecipeDumper;
import com.almostreliable.unified.recipe.RecipeTransformer;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.Collection;
import java.util.Map;

public final class AlmostUnifiedRuntimeImpl implements AlmostUnifiedRuntime {

    private final Collection<? extends UnifyHandler> unifyHandlers;
    private final DuplicationConfig duplicationConfig;
    private final DebugConfig debugConfig;
    private final UnifierRegistry unifierRegistry;

    AlmostUnifiedRuntimeImpl(
            Collection<? extends UnifyHandler> unifyHandlers,
            DuplicationConfig duplicationConfig,
            DebugConfig debugConfig,
            UnifierRegistry unifierRegistry
    ) {
        this.unifyHandlers = unifyHandlers;
        this.duplicationConfig = duplicationConfig;
        this.debugConfig = debugConfig;
        this.unifierRegistry = unifierRegistry;
    }

    @Override
    public void run(Map<ResourceLocation, JsonElement> recipes, boolean skipClientTracking) {
        debugConfig.logRecipes(recipes, "recipes_before_unification.txt");
//        debugConfig.logUnifyTagDump(tagMap); // TODO

        long startTime = System.currentTimeMillis();
        RecipeTransformer.Result result = new RecipeTransformer(
                unifierRegistry,
                unifyHandlers,
                duplicationConfig
        ).transformRecipes(recipes, skipClientTracking);
        RecipeDumper dumper = new RecipeDumper(result, startTime, System.currentTimeMillis());
        dumper.dump(debugConfig.dumpOverview, debugConfig.dumpUnification, debugConfig.dumpDuplicates);

        debugConfig.logRecipes(recipes, "recipes_after_unification.txt");
    }

    @Override
    public TagMap<Item> getFilteredTagMap() {
        return null; // TODO
    }

    @Override
    public ReplacementMap getReplacementMap() {
        return null; // TODO
    }

    @Override
    public UnifySettings getUnifyConfig() {
        return null; // TODO
    }
}

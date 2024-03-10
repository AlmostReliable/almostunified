package com.almostreliable.unified;

import com.almostreliable.unified.api.*;
import com.almostreliable.unified.config.DebugConfig;
import com.almostreliable.unified.config.DuplicationConfig;
import com.almostreliable.unified.impl.CompositeReplacementMap;
import com.almostreliable.unified.recipe.RecipeDumper;
import com.almostreliable.unified.recipe.RecipeTransformer;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public final class AlmostUnifiedRuntimeImpl implements AlmostUnifiedRuntime {

    private final TagMap<Item> tagMap;
    private final Collection<? extends UnifyHandler> unifyHandlers;
    private final DuplicationConfig duplicationConfig;
    private final DebugConfig debugConfig;
    private final UnifierRegistry unifierRegistry;
    private final TagOwnerships tagOwnerships;
    private final ReplacementMap compositeReplacementMap;

    AlmostUnifiedRuntimeImpl(TagMap<Item> tagMap, Collection<? extends UnifyHandler> unifyHandlers, DuplicationConfig duplicationConfig, DebugConfig debugConfig, UnifierRegistry unifierRegistry, TagOwnerships tagOwnerships) {
        this.tagMap = tagMap;
        this.unifyHandlers = unifyHandlers;
        this.duplicationConfig = duplicationConfig;
        this.debugConfig = debugConfig;
        this.unifierRegistry = unifierRegistry;
        this.tagOwnerships = tagOwnerships;
        this.compositeReplacementMap = new CompositeReplacementMap(unifyHandlers, tagOwnerships);
    }

    @Override
    public void run(Map<ResourceLocation, JsonElement> recipes, boolean skipClientTracking) {
        debugConfig.logRecipes(recipes, "recipes_before_unification.txt");
//        debugConfig.logUnifyTagDump(tagMap); // TODO

        long startTime = System.currentTimeMillis();
        RecipeTransformer.Result result = new RecipeTransformer(unifierRegistry,
                unifyHandlers,
                duplicationConfig).transformRecipes(recipes, skipClientTracking);
        RecipeDumper dumper = new RecipeDumper(result, startTime, System.currentTimeMillis());
        dumper.dump(debugConfig.dumpOverview, debugConfig.dumpUnification, debugConfig.dumpDuplicates);

        debugConfig.logRecipes(recipes, "recipes_after_unification.txt");
    }

    @Override
    public TagMap<Item> getTagMap() {
        return tagMap;
    }

    @Override
    public ReplacementMap getReplacementMap() {
        return compositeReplacementMap;
    }

    @Override
    public Collection<? extends UnifyHandler> getUnifyHandlers() {
        return Collections.unmodifiableCollection(unifyHandlers);
    }

    @Nullable
    @Override
    public UnifyHandler getUnifyHandler(String name) {
        for (UnifyHandler unifyHandler : unifyHandlers) {
            if (unifyHandler.getName().equals(name)) {
                return unifyHandler;
            }
        }

        return null;
    }

    @Override
    public TagOwnerships getTagOwnerships() {
        return tagOwnerships;
    }
}

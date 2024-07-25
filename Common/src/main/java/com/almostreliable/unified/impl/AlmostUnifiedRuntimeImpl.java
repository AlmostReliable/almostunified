package com.almostreliable.unified.impl;

import com.almostreliable.unified.api.*;
import com.almostreliable.unified.config.DebugConfig;
import com.almostreliable.unified.config.DuplicateConfig;
import com.almostreliable.unified.recipe.RecipeDumper;
import com.almostreliable.unified.recipe.RecipeTransformer;
import com.almostreliable.unified.recipe.RecipeUnifyHandler;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public final class AlmostUnifiedRuntimeImpl implements AlmostUnifiedRuntime, RecipeUnifyHandler {

    private final Collection<? extends UnifyHandler> unifyHandlers;
    private final DuplicateConfig duplicateConfig;
    private final DebugConfig debugConfig;
    private final RecipeUnifierRegistry recipeUnifierRegistry;
    private final TagOwnerships tagOwnerships;
    private final Placeholders placeholders;
    private final UnifyLookup compositeUnifyLookup;

    public AlmostUnifiedRuntimeImpl(Collection<? extends UnifyHandler> unifyHandlers, DuplicateConfig duplicateConfig, DebugConfig debugConfig, RecipeUnifierRegistry recipeUnifierRegistry, TagOwnerships tagOwnerships, Placeholders placeholders) {
        this.unifyHandlers = unifyHandlers;
        this.duplicateConfig = duplicateConfig;
        this.debugConfig = debugConfig;
        this.recipeUnifierRegistry = recipeUnifierRegistry;
        this.tagOwnerships = tagOwnerships;
        this.placeholders = placeholders;
        this.compositeUnifyLookup = new CompositeUnifyLookup(unifyHandlers, tagOwnerships);
    }

    @Override
    public void run(Map<ResourceLocation, JsonElement> recipes, boolean skipClientTracking) {
        debugConfig.logRecipes(recipes, "recipes_before_unification.txt");
        debugConfig.logUnifyTagDump(getUnifyLookup());

        long startTime = System.currentTimeMillis();
        RecipeTransformer.Result result = new RecipeTransformer(recipeUnifierRegistry,
                unifyHandlers,
                duplicateConfig).transformRecipes(recipes, skipClientTracking);
        RecipeDumper dumper = new RecipeDumper(result, startTime, System.currentTimeMillis());
        dumper.dump(debugConfig.dumpOverview, debugConfig.dumpUnification, debugConfig.dumpDuplicates);

        debugConfig.logRecipes(recipes, "recipes_after_unification.txt");
    }

    @Override
    public UnifyLookup getUnifyLookup() {
        return compositeUnifyLookup;
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

    @Override
    public Placeholders getPlaceholders() {
        return placeholders;
    }
}

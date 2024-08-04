package com.almostreliable.unified.impl;

import com.almostreliable.unified.api.*;
import com.almostreliable.unified.recipe.RecipeTransformer;
import com.almostreliable.unified.recipe.RecipeUnifyHandler;
import com.almostreliable.unified.utils.DebugHandler;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public final class AlmostUnifiedRuntimeImpl implements AlmostUnifiedRuntime, RecipeUnifyHandler {

    private final Collection<? extends UnifyHandler> unifyHandlers;
    private final RecipeUnifierRegistry recipeUnifierRegistry;
    private final TagSubstitutions tagSubstitutions;
    private final Placeholders placeholders;
    private final UnifyLookup compositeUnifyLookup;

    public AlmostUnifiedRuntimeImpl(Collection<? extends UnifyHandler> unifyHandlers, RecipeUnifierRegistry recipeUnifierRegistry, TagSubstitutions tagSubstitutions, Placeholders placeholders) {
        this.unifyHandlers = unifyHandlers;
        this.recipeUnifierRegistry = recipeUnifierRegistry;
        this.tagSubstitutions = tagSubstitutions;
        this.placeholders = placeholders;
        this.compositeUnifyLookup = new CompositeUnifyLookup(unifyHandlers, tagSubstitutions);
    }

    @Override
    public void run(Map<ResourceLocation, JsonElement> recipes) {
        DebugHandler debugHandler = DebugHandler.onRunStart(recipes, compositeUnifyLookup);

        debugHandler.measure(() -> {
            var transformer = new RecipeTransformer(recipeUnifierRegistry, unifyHandlers);
            return transformer.transformRecipes(recipes);
        });

        debugHandler.onRunEnd(recipes);
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
    public TagSubstitutions getTagSubstitutions() {
        return tagSubstitutions;
    }

    @Override
    public Placeholders getPlaceholders() {
        return placeholders;
    }
}

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
    private final TagOwnerships tagOwnerships;
    private final Placeholders placeholders;
    private final UnifyLookup compositeUnifyLookup;

    public AlmostUnifiedRuntimeImpl(Collection<? extends UnifyHandler> unifyHandlers, RecipeUnifierRegistry recipeUnifierRegistry, TagOwnerships tagOwnerships, Placeholders placeholders) {
        this.unifyHandlers = unifyHandlers;
        this.recipeUnifierRegistry = recipeUnifierRegistry;
        this.tagOwnerships = tagOwnerships;
        this.placeholders = placeholders;
        this.compositeUnifyLookup = new CompositeUnifyLookup(unifyHandlers, tagOwnerships);
    }

    @Override
    public void run(Map<ResourceLocation, JsonElement> recipes, boolean skipClientTracking) {
        DebugHandler debugHandler = DebugHandler.onRunStart(recipes, compositeUnifyLookup);

        debugHandler.measure(() -> {
            var transformer = new RecipeTransformer(recipeUnifierRegistry, unifyHandlers);
            return transformer.transformRecipes(recipes, skipClientTracking);
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
    public TagOwnerships getTagOwnerships() {
        return tagOwnerships;
    }

    @Override
    public Placeholders getPlaceholders() {
        return placeholders;
    }
}

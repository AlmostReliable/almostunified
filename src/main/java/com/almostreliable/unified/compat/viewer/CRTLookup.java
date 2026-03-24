package com.almostreliable.unified.compat.viewer;

import com.almostreliable.unified.utils.Utils;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.Nullable;

public final class CRTLookup {

    private CRTLookup() {}

    @Nullable
    public static ClientRecipeTracker.ClientRecipeLink getLink(Identifier recipeId) {
        Identifier link = Utils.getRL(recipeId.getNamespace());
        if (Minecraft.getInstance().level == null) {
            return null;
        }

        // return Minecraft.getInstance().level
        //     .getRecipeManager()
        //     .byKey(link)
        //     .map(RecipeHolder::value)
        //     .filter(ClientRecipeTracker.class::isInstance)
        //     .map(ClientRecipeTracker.class::cast)
        //     .map(tracker -> tracker.getLink(recipeId))
        //     .orElse(null);
        return null;
    }
}

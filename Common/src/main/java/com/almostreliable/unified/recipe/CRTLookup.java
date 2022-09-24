package com.almostreliable.unified.recipe;

import com.almostreliable.unified.BuildConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public final class CRTLookup {

    private CRTLookup() {}

    @Nullable
    public static ClientRecipeTracker.ClientRecipeLink getLink(ResourceLocation recipeId) {
        ResourceLocation linkRecipe = new ResourceLocation(BuildConfig.MOD_ID, recipeId.getNamespace());
        if (Minecraft.getInstance().level == null) {
            return null;
        }

        return Minecraft.getInstance().level
                .getRecipeManager()
                .byKey(linkRecipe)
                .filter(ClientRecipeTracker.class::isInstance)
                .map(ClientRecipeTracker.class::cast)
                .map(tracker -> tracker.getLink(recipeId))
                .orElse(null);
    }
}

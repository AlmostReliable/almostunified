package com.almostreliable.unified.recipe;

import com.almostreliable.unified.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

import javax.annotation.Nullable;

public final class CRTLookup {

    private CRTLookup() {}

    @Nullable
    public static ClientRecipeTracker.ClientRecipeLink getLink(ResourceLocation recipeId) {
        ResourceLocation link = Utils.getRL(recipeId.getNamespace());
        if (Minecraft.getInstance().level == null) {
            return null;
        }

        return Minecraft.getInstance().level
                .getRecipeManager()
                .byKey(link)
                .map(RecipeHolder::value)
                .filter(ClientRecipeTracker.class::isInstance)
                .map(ClientRecipeTracker.class::cast)
                .map(tracker -> tracker.getLink(recipeId))
                .orElse(null);
    }
}

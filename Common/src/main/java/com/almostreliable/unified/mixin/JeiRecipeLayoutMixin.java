package com.almostreliable.unified.mixin;

import com.almostreliable.unified.compat.RecipeIndicator;
import com.almostreliable.unified.recipe.CRTLookup;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.gui.recipes.layout.RecipeLayout;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(RecipeLayout.class)
public abstract class JeiRecipeLayoutMixin<R> {

    @Shadow @Final
    private IRecipeCategory<R> recipeCategory;
    @Shadow @Final
    private R recipe;

    @Inject(method = "drawRecipe", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD, remap = false)
    private void unified$drawRecipe(PoseStack stack, int mouseX, int mouseY, CallbackInfo ci, IDrawable background, int mX, int mY, IDrawable categoryBackground, int width, int height) {
        var recipeId = recipeCategory.getRegistryName(recipe);
        if (recipeId == null) return;

        var link = CRTLookup.getLink(recipeId);
        if (link == null) return;

        RecipeIndicator.handleIndicator(stack, width, height, mX, mY, link);
    }
}

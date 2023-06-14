package com.almostreliable.unified.mixin.compat;

import com.almostreliable.unified.compat.AlmostJEI;
import com.almostreliable.unified.compat.RecipeIndicator;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.library.gui.recipes.RecipeLayout;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(RecipeLayout.class)
public abstract class JeiRecipeLayoutMixin<R> {

    @Shadow(remap = false) @Final
    private IRecipeCategory<R> recipeCategory;
    @Shadow(remap = false) @Final
    private R recipe;

    @Inject(method = "drawRecipe", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD)
    private void unified$catchLayoutInfo(PoseStack stack, int mouseX, int mouseY, CallbackInfo ci, IDrawable background, int mX, int mY, int x, int y) {
        var posX = x - RecipeIndicator.RENDER_SIZE;
        var posY = y - RecipeIndicator.RENDER_SIZE;
        AlmostJEI.handleIndicator(stack, mX, mY, posX, posY, recipeCategory, recipe);
    }
}

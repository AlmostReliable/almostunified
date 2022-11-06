package com.almostreliable.unified.mixin;

import com.almostreliable.unified.compat.AlmostJEI;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.gui.recipes.layout.RecipeLayout;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Pseudo
@Mixin(RecipeLayout.class)
public abstract class Jei10RecipeLayoutMixin<R> {

    @Shadow(remap = false) @Final
    private static int RECIPE_BORDER_PADDING;
    @Shadow(remap = false) @Final
    private IRecipeCategory<R> recipeCategory;
    @Shadow(remap = false) @Final
    private R recipe;

    @Inject(method = "drawRecipe", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD)
    private void unified$drawRecipe(PoseStack stack, int mouseX, int mouseY, CallbackInfo ci, IDrawable background, int mX, int mY, IDrawable categoryBackground, int x, int y) {
        AlmostJEI.handleIndicator(stack, mX, mY, x, y, recipeCategory, recipe, RECIPE_BORDER_PADDING);
    }
}

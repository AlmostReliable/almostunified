package com.almostreliable.unified.mixin;

import com.almostreliable.unified.compat.AlmostJEI;
import com.almostreliable.unified.compat.RecipeIndicator;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.gui.recipes.layout.RecipeLayout;
import mezz.jei.common.gui.recipes.layout.RecipeTransferButton;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nullable;

@Mixin(RecipeLayout.class)
public abstract class JeiRecipeLayoutMixin<R> {

    @Shadow(remap = false) @Final
    private IRecipeCategory<R> recipeCategory;
    @Shadow(remap = false) @Final
    private R recipe;
    @Shadow(remap = false) @Final
    @Nullable private RecipeTransferButton recipeTransferButton;

    @Unique
    private boolean handled;

    @Inject(method = "drawRecipe", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD)
    private void unified$drawCornerIndicator(PoseStack stack, int mouseX, int mouseY, CallbackInfo ci, IDrawable background, int mX, int mY, IDrawable categoryBackground, int x, int y) {
        if (recipeTransferButton != null && recipeTransferButton.visible) return;
        var posX = x - RecipeIndicator.RENDER_SIZE;
        var posY = y - RecipeIndicator.RENDER_SIZE;
        AlmostJEI.handleIndicator(stack, mX, mY, posX, posY, recipeCategory, recipe);
        handled = true;
    }

    @Inject(method = "drawRecipe", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getInstance()Lnet/minecraft/client/Minecraft;"))
    private void unified$drawButtonIndicator(PoseStack stack, int mouseX, int mouseY, CallbackInfo ci) {
        assert recipeTransferButton != null;
        if (handled) {
            handled = false;
            return;
        }
        var posX = recipeTransferButton.x + (RecipeTransferButton.RECIPE_BUTTON_SIZE - RecipeIndicator.RENDER_SIZE) / 2;
        var posY = recipeTransferButton.y - RecipeIndicator.RENDER_SIZE - 2;
        AlmostJEI.handleIndicator(stack, mouseX, mouseY, posX, posY, recipeCategory, recipe);
    }
}

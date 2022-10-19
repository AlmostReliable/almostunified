package com.almostreliable.unified.mixin;

import com.almostreliable.unified.compat.RecipeIndicator;
import com.almostreliable.unified.recipe.CRTLookup;
import com.almostreliable.unified.utils.Utils;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.gui.recipes.RecipeLayout;
import net.minecraft.client.renderer.Rect2i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@OnlyIn(Dist.CLIENT)
@Mixin(RecipeLayout.class)
public abstract class JeiRecipeLayoutMixin<R> {

    @Shadow(remap = false) @Final
    private static int RECIPE_BORDER_PADDING;
    @Shadow(remap = false) @Final
    private IRecipeCategory<R> recipeCategory;
    @Shadow(remap = false) @Final
    private R recipe;

    @Inject(method = "drawRecipe", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD)
    private void unified$drawRecipe(PoseStack stack, int mouseX, int mouseY, CallbackInfo ci, IDrawable background, int mX, int mY, IDrawable categoryBackground, int x, int y) {
        var recipeId = recipeCategory.getRegistryName(recipe);
        if (recipeId == null) return;

        var link = CRTLookup.getLink(recipeId);
        if (link == null) return;

        var posX = x - RecipeIndicator.SIZE / 2 - RECIPE_BORDER_PADDING + 1;
        var posY = y - RecipeIndicator.SIZE / 2 - RECIPE_BORDER_PADDING + 1;
        var area = new Rect2i(posX, posY, 10, 10);
        RecipeIndicator.renderIndicator(stack, area);
        if (mX >= area.getX() && mX <= area.getX() + area.getWidth() &&
            mY >= area.getY() && mY <= area.getY() + area.getHeight()) {
            Utils.renderTooltip(stack, mX, mY, RecipeIndicator.constructTooltip(link));
        }
    }
}

package com.almostreliable.unified.mixin;

import com.almostreliable.unified.compat.AlmostJEI;
import com.almostreliable.unified.compat.RecipeIndicator;
import com.almostreliable.unified.utils.Utils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.recipes.RecipeTransferButton;
import mezz.jei.gui.recipes.RecipesGui;
import net.minecraft.client.renderer.Rect2i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Mixin(RecipesGui.class)
public abstract class JeiRecipesGuiMixin {

    private static final int BORDER_PADDING = 2;

    @Shadow(remap = false) @Final
    private List<RecipeTransferButton> recipeTransferButtons;

    @Inject(
            method = "drawLayouts",
            at = @At(value = "INVOKE", target = "Lmezz/jei/api/gui/IRecipeLayoutDrawable;drawRecipe(Lcom/mojang/blaze3d/vertex/PoseStack;II)V", shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILHARD,
            remap = false
    )
    private void unified$drawIndicator(
            PoseStack poseStack, int mX, int mY, CallbackInfoReturnable<Optional<IRecipeLayoutDrawable<?>>> cir,
            IRecipeLayoutDrawable<?> hoveredLayout, Iterator<?> i, IRecipeLayoutDrawable<?> recipeLayout
    ) {
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();

        var buttonArea = recipeLayout.getRecipeTransferButtonArea();

        RecipeTransferButton transferButton = null;
        for (var button : recipeTransferButtons) {
            if (!button.visible || !buttonsMatch(buttonArea, button.getArea())) continue;
            transferButton = button;
        }

        int posX;
        int posY;
        if (transferButton == null) {
            var layoutArea = recipeLayout.getRect();
            posX = layoutArea.getX() + layoutArea.getWidth() - RecipeIndicator.RENDER_SIZE / 2;
            posY = layoutArea.getY() + layoutArea.getHeight() - RecipeIndicator.RENDER_SIZE / 2 + BORDER_PADDING;
        } else {
            posX = buttonArea.getX() + BORDER_PADDING;
            posY = buttonArea.getY() - RecipeIndicator.RENDER_SIZE - 2 + BORDER_PADDING;
        }

        AlmostJEI.handleIndicator(
                poseStack,
                mX,
                mY,
                posX,
                posY,
                Utils.cast(recipeLayout.getRecipeCategory()),
                recipeLayout.getRecipe()
        );
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
    }

    private static boolean buttonsMatch(Rect2i a, ImmutableRect2i b) {
        return a.getX() == b.getX() &&
               a.getY() == b.getY() &&
               a.getWidth() == b.getWidth() &&
               a.getHeight() == b.getHeight();
    }
}

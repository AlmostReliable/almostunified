package com.almostreliable.unified.mixin;

import com.almostreliable.unified.compat.AlmostJEI;
import com.almostreliable.unified.compat.RecipeIndicator;
import com.almostreliable.unified.utils.Utils;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.gui.recipes.RecipeTransferButton;
import mezz.jei.gui.recipes.RecipesGui;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

@Mixin(RecipesGui.class)
public abstract class JeiRecipesGuiMixin {

    @Unique
    private static final Map<ResourceLocation, RecipeIndicator.RenderEntry> RENDER_QUEUE = new HashMap<>();

    @Inject(method = "drawLayouts", at = @At(value = "INVOKE", target = "Lmezz/jei/gui/recipes/RecipeTransferButton;render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void unified$setupIndicators(PoseStack stack, int mX, int mY, CallbackInfoReturnable<Optional<IRecipeLayoutDrawable<?>>> cir, IRecipeLayoutDrawable<?> hoveredLayout, Minecraft mc, float partial, Iterator<?> iterator, RecipeTransferButton button) {
        var recipeLayout = ((JeiRecipeTransferButtonAccessor) button).getRecipeLayout();
        var recipeId = recipeLayout.getRecipeCategory().getRegistryName(Utils.cast(recipeLayout.getRecipe()));
        if (recipeId == null) return;

        int posX;
        int posY;
        if (button.visible) {
            posX = button.x + (button.getWidth() - RecipeIndicator.RENDER_SIZE) / 2;
            posY = button.y - RecipeIndicator.RENDER_SIZE - 2;
        } else {
            var area = recipeLayout.getRect();
            posX = area.getX() + area.getWidth() - RecipeIndicator.RENDER_SIZE / 2 + 2;
            posY = area.getY() + area.getHeight() - RecipeIndicator.RENDER_SIZE / 2 + 2;
        }

        RENDER_QUEUE.put(recipeId, new RecipeIndicator.RenderEntry(posX, posY));
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lmezz/jei/gui/recipes/RecipeGuiTabs;draw(Lnet/minecraft/client/Minecraft;Lcom/mojang/blaze3d/vertex/PoseStack;IILmezz/jei/api/helpers/IModIdHelper;)V"))
    private void unified$flushQueue(PoseStack stack, int mX, int mY, float partial, CallbackInfo ci) {
        RENDER_QUEUE.forEach((recipeId, entry) -> {
            AlmostJEI.handleIndicator(stack, mX, mY, entry.pX(), entry.pY(), recipeId);
        });
        RENDER_QUEUE.clear();
    }
}

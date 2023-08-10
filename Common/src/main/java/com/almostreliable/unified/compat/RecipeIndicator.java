package com.almostreliable.unified.compat;

import com.almostreliable.unified.recipe.ClientRecipeTracker.ClientRecipeLink;
import com.almostreliable.unified.utils.Utils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class RecipeIndicator {

    public static final int RENDER_SIZE = 10;
    private static final int TEXTURE_SIZE = 16;
    private static final ResourceLocation TEXTURE = Utils.getRL("textures/ingot.png");

    private RecipeIndicator() {}

    static List<Component> constructTooltip(ClientRecipeLink link) {
        var unified = Component.translatable(Utils.prefix("unified")).append(": ")
                .withStyle(c -> c.withColor(ChatFormatting.AQUA));
        unified.append(Component.translatable(Utils.prefix(link.isUnified() ? "yes" : "no"))
                .withStyle(c -> c.withColor(ChatFormatting.WHITE)));

        var duplicate = Component.translatable(Utils.prefix("duplicate")).append(": ")
                .withStyle(c -> c.withColor(ChatFormatting.AQUA));
        duplicate.append(Component.translatable(Utils.prefix(link.isDuplicate() ? "yes" : "no"))
                .withStyle(c -> c.withColor(ChatFormatting.WHITE)));

        return List.of(
                Component.translatable(Utils.prefix("description")).withStyle(c -> c.withColor(ChatFormatting.GOLD)),
                Component.literal(""),
                unified,
                duplicate,
                Component.literal(""),
                Component.translatable(Utils.prefix("warning")).withStyle(c -> c.withColor(ChatFormatting.RED))
        );
    }

    static void renderIndicator(PoseStack poseStack, Rect2i area) {
        poseStack.pushPose();
        poseStack.translate(area.getX(), area.getY(), 0);
        var scale = area.getWidth() / (float) TEXTURE_SIZE;
        poseStack.scale(scale, scale, scale);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        GuiComponent.blit(poseStack, 0, 0, 0, 0, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);
        poseStack.popPose();
    }
}

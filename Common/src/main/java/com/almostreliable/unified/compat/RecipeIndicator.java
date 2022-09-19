package com.almostreliable.unified.compat;

import com.almostreliable.unified.recipe.ClientRecipeTracker;
import com.almostreliable.unified.utils.Utils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class RecipeIndicator {

    private static final ResourceLocation TEXTURE = Utils.getRL("textures/ingot.png");
    private static final int SIZE = 16;
    private static final float SCALE = 0.6f;

    private RecipeIndicator() {}

    public static void handleIndicator(PoseStack poseStack, int width, int height, int mX, int mY, ClientRecipeTracker.ClientRecipeLink link) {
        var x = calculatePos(width) - 2;
        var y = calculatePos(height) - 2;

        renderIndicator(poseStack, x, y);

        if (mX >= x && mX <= x + SIZE * SCALE && mY >= y && mY <= y + SIZE * SCALE) {
            renderTooltip(poseStack, mX, mY, constructTooltip(link));
        }
    }

    private static int calculatePos(int value) {
        return value - SIZE / 2;
    }

    public static List<Component> constructTooltip(ClientRecipeTracker.ClientRecipeLink link) {
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

    private static void renderIndicator(PoseStack poseStack, int x, int y) {
        poseStack.pushPose();
        poseStack.translate(x, y, 0);
        poseStack.scale(SCALE, SCALE, SCALE);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        GuiComponent.blit(poseStack, 0, 0, 0, 0, SIZE, SIZE, SIZE, SIZE);
        poseStack.popPose();
    }

    private static void renderTooltip(PoseStack poseStack, int mouseX, int mouseY, List<Component> tooltip) {
        var screen = Minecraft.getInstance().screen;
        if (screen == null) return;
        screen.renderComponentTooltip(poseStack, tooltip, mouseX, mouseY);
    }
}

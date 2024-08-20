package com.almostreliable.unified.compat.viewer;

import com.almostreliable.unified.compat.viewer.ClientRecipeTracker.ClientRecipeLink;
import com.almostreliable.unified.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

final class RecipeIndicator {

    static final int RENDER_SIZE = 10;
    private static final int TEXTURE_SIZE = 16;
    private static final ResourceLocation TEXTURE = Utils.getRL("textures/ingot.png");

    private RecipeIndicator() {}

    static void renderIndicator(GuiGraphics guiGraphics, int pX, int pY, int size) {
        var poseStack = guiGraphics.pose();
        poseStack.pushPose();

        poseStack.translate(pX, pY, 0);
        var scale = size / (float) TEXTURE_SIZE;
        poseStack.scale(scale, scale, scale);
        guiGraphics.blit(TEXTURE, 0, 0, 0, 0, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);

        poseStack.popPose();
    }

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
                Component.literal(" "),
                unified,
                duplicate,
                Component.literal(" "),
                Component.translatable(Utils.prefix("warning")).withStyle(c -> c.withColor(ChatFormatting.RED))
        );
    }

    static void renderTooltip(GuiGraphics guiGraphics, ClientRecipeLink link, double mouseX, double mouseY) {
        var mc = Minecraft.getInstance();
        var font = mc.font;
        var screen = mc.screen;
        if (screen == null) return;

        var tooltip = constructTooltip(link).stream()
                .map(c -> font.split(c, screen.width - (int) mouseX - 200))
                .flatMap(List::stream)
                .toList();

        guiGraphics.renderTooltip(
                font,
                tooltip,
                (int) mouseX,
                (int) mouseY
        );
    }
}

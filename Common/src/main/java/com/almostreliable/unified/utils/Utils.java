package com.almostreliable.unified.utils;

import com.almostreliable.unified.BuildConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class Utils {
    public static final ResourceLocation UNUSED_ID = new ResourceLocation(BuildConfig.MOD_ID, "unused_id");
    public static final UnifyTag<Item> UNUSED_TAG = UnifyTag.item(UNUSED_ID);

    private Utils() {}

    public static UnifyTag<Item> toItemTag(@Nullable String tag) {
        if (tag == null) {
            return UNUSED_TAG;
        }

        ResourceLocation rl = ResourceLocation.tryParse(tag);
        if (rl == null) {
            return UNUSED_TAG;
        }

        return UnifyTag.item(rl);
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object o) {
        return (T) o;
    }

    public static ResourceLocation getRL(String path) {
        return new ResourceLocation(BuildConfig.MOD_ID, path);
    }

    public static String prefix(String path) {
        return BuildConfig.MOD_ID + "." + path;
    }

    public static void renderTooltip(PoseStack stack, int mX, int mY, List<Component> tooltip) {
        var mc = Minecraft.getInstance();
        var screen = mc.screen;
        if (screen == null) return;

        List<FormattedCharSequence> formattedTooltip = new ArrayList<>(tooltip.size());
        for (Component line : tooltip) {
            formattedTooltip.addAll(ComponentRenderUtils.wrapComponents(line, 300, mc.font));
        }
        screen.renderTooltip(stack, formattedTooltip, mX, mY);
    }
}

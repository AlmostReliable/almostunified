package com.almostreliable.unified.utils;

import com.almostreliable.unified.BuildConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;

public class Utils {
    public static final ResourceLocation UNUSED_ID = new ResourceLocation(BuildConfig.MOD_ID, "unused_id");
    public static final UnifyTag<Item> UNUSED_TAG = UnifyTag.item(UNUSED_ID);

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
}

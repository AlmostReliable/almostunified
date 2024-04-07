package com.almostreliable.unified.utils;

import com.almostreliable.unified.BuildConfig;
import com.almostreliable.unified.api.UnifyEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.Set;

public final class Utils {
    public static final ResourceLocation UNUSED_ID = new ResourceLocation(BuildConfig.MOD_ID, "unused_id");
    public static final TagKey<Item> UNUSED_TAG = TagKey.create(Registries.ITEM, UNUSED_ID);

    private Utils() {}

    public static TagKey<Item> toItemTag(@Nullable String tag) {
        if (tag == null) {
            return UNUSED_TAG;
        }

        ResourceLocation rl = ResourceLocation.tryParse(tag);
        if (rl == null) {
            return UNUSED_TAG;
        }

        return TagKey.create(Registries.ITEM, rl);
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

    /**
     * Checks if all ids have the same namespace
     *
     * @param ids set of ids
     * @return true if all ids have the same namespace
     */
    public static boolean allSameNamespace(Set<UnifyEntry<Item>> ids) {
        if (ids.size() <= 1) return true;

        var it = ids.iterator();
        var namespace = it.next().id().getNamespace();

        while (it.hasNext()) {
            if (!it.next().id().getNamespace().equals(namespace)) return false;
        }

        return true;
    }
}

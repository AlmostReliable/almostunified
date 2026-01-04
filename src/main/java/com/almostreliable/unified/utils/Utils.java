package com.almostreliable.unified.utils;

import com.almostreliable.unified.api.constant.ModConstants;
import com.almostreliable.unified.api.unification.UnificationEntry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.Collection;

public final class Utils {

    public static final Identifier UNUSED_ID = getRL("unused_id");
    public static final TagKey<Item> UNUSED_TAG = TagKey.create(Registries.ITEM, UNUSED_ID);

    private Utils() {}

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object o) {
        return (T) o;
    }

    public static Identifier getRL(String path) {
        return Identifier.fromNamespaceAndPath(ModConstants.ALMOST_UNIFIED, path);
    }

    public static String prefix(String path) {
        return ModConstants.ALMOST_UNIFIED + "." + path;
    }

    /**
     * Checks if all ids have the same namespace
     *
     * @param ids set of ids
     * @return true if all ids have the same namespace
     */
    public static boolean allSameNamespace(Collection<UnificationEntry<Item>> ids) {
        if (ids.size() <= 1) return true;

        var it = ids.iterator();
        var namespace = it.next().id().getNamespace();

        while (it.hasNext()) {
            if (!it.next().id().getNamespace().equals(namespace)) return false;
        }

        return true;
    }
}

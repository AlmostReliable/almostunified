package com.almostreliable.unified.utils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.Set;

public record UnifyTag<T>(Class<T> boundType, ResourceLocation location, Set<ResourceLocation> delegates) {
    public static UnifyTag<Item> item(ResourceLocation location) {
        return new UnifyTag<>(Item.class, location, Set.of());
    }

    public static UnifyTag<Item> item(ResourceLocation location, Set<ResourceLocation> delegates) {
        return new UnifyTag<>(Item.class, location, delegates);
    }

    @Override
    public String toString() {
        return "UnifyTag[" + boundType.getSimpleName().toLowerCase() + " / " + location + "]";
    }
}

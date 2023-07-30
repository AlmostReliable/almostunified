package com.almostreliable.unified.utils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public record UnifyTag<T>(Class<T> boundType, ResourceLocation location) {
    public static UnifyTag<Item> item(ResourceLocation location) {
        return new UnifyTag<>(Item.class, location);
    }

    public static UnifyTag<Block> block(ResourceLocation location) {
        return new UnifyTag<>(Block.class, location);
    }

    @Override
    public String toString() {
        return "UnifyTag[" + boundType.getSimpleName().toLowerCase() + " / " + location + "]";
    }
}
